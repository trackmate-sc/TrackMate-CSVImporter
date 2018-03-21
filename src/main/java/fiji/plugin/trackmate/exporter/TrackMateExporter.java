package fiji.plugin.trackmate.exporter;

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FRAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_ID_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_NAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_QUALITY_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_RADIUS_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_TRACK_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_COLUMN_NAME;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.detection.ManualDetectorFactory;
import fiji.plugin.trackmate.features.edges.EdgeAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeTargetAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeTimeLocationAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeVelocityAnalyzer;
import fiji.plugin.trackmate.features.manual.ManualEdgeColorAnalyzer;
import fiji.plugin.trackmate.features.manual.ManualSpotColorAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotAnalyzerFactory;
import fiji.plugin.trackmate.features.track.TrackAnalyzer;
import fiji.plugin.trackmate.features.track.TrackDurationAnalyzer;
import fiji.plugin.trackmate.features.track.TrackIndexAnalyzer;
import fiji.plugin.trackmate.features.track.TrackLocationAnalyzer;
import fiji.plugin.trackmate.features.track.TrackSpeedStatisticsAnalyzer;
import fiji.plugin.trackmate.features.track.TrackSpotQualityFeatureAnalyzer;
import fiji.plugin.trackmate.providers.EdgeAnalyzerProvider;
import fiji.plugin.trackmate.providers.SpotAnalyzerProvider;
import fiji.plugin.trackmate.providers.TrackAnalyzerProvider;
import fiji.plugin.trackmate.tracking.ManualTrackerFactory;
import ij.ImagePlus;

public class TrackMateExporter
{

	private String errorMessage;

	private Logger logger;

	public TrackMateExporter()
	{
		this( Logger.VOID_LOGGER );
	}

	public TrackMateExporter( final Logger logger )
	{
		this.logger = logger;
	}

	public Model importModel( final String filePath, final Map< String, Integer > fieldMap, final double radius, final String spaceUnits, final String timeUnits, final double frameInterval )
	{
		@SuppressWarnings( "unused" )
		CSVMetadata csvMetadata = null;
		try
		{
			csvMetadata = CSVMetadata.parse( filePath );
		}
		catch ( final IOException e )
		{
			errorMessage = e.getMessage();
			e.printStackTrace();
			return null;
		}

		/*
		 * Parse mandatory headers.
		 */

		final Integer xcol = fieldMap.get( KEY_X_COLUMN_NAME );
		final Integer ycol = fieldMap.get( KEY_Y_COLUMN_NAME );
		final Integer zcol = fieldMap.get( KEY_Z_COLUMN_NAME );
		final Integer framecol = fieldMap.get( KEY_FRAME_COLUMN_NAME );

		/*
		 * Parse optional headers.
		 */

		final Integer qualitycol = fieldMap.get( KEY_QUALITY_COLUMN_NAME );
		final boolean importQuality = qualitycol != null;

		final Integer namecol = fieldMap.get( KEY_NAME_COLUMN_NAME );
		final boolean importName = namecol != null;

		final Integer idcol = fieldMap.get( KEY_ID_COLUMN_NAME );
		final boolean importID = idcol != null;

		final Integer trackcol = fieldMap.get( KEY_TRACK_COLUMN_NAME );
		final boolean importTrack = trackcol != null;

		final Integer radiuscol = fieldMap.get( KEY_RADIUS_COLUMN_NAME );
		final boolean importRadius = radiuscol != null;

		/*
		 * Prepare spot & track collections.
		 */

		final Map< Integer, Set< Spot > > spots = new HashMap<>();
		final Map< Integer, List< Spot > > tracks = new HashMap<>();

		/*
		 * Open and parse file.
		 */

		Reader in;
		CSVParser records;
		try
		{
			in = new FileReader( filePath );
		}
		catch ( final FileNotFoundException e )
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
			return null;
		}

		try
		{
			final CSVFormat csvFormat = CSVFormat.EXCEL
					.withHeader()
					.withCommentMarker( '#' );
			records = csvFormat.parse( in );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
			return null;
		}

		/*
		 * Iterate over records.
		 */

		logger.log( String.format( "Parsing records.\n" ) );
		long nRecords = 0;
		for ( final CSVRecord record : records )
		{
			nRecords++;
			try
			{
				final double x = Double.parseDouble( record.get( xcol ) );
				final double y = Double.parseDouble( record.get( ycol ) );
				final double z = Double.parseDouble( record.get( zcol ) );
				// 1-based to 0-based.
				final int t = Integer.parseInt( record.get( framecol ) );

				double q = 1.;
				if ( importQuality )
					q = Double.parseDouble( record.get( qualitycol ) );

				double r = radius;
				if ( importRadius )
					r = Double.parseDouble( record.get( radiuscol ) );

				String name = null;
				if ( importName )
					name = record.get( namecol );

				final Spot spot;
				if ( importID )
				{
					// Hijack spot IDs: we force ID to match ID provided.
					final int id = Integer.parseInt( record.get( idcol ) );
					spot = new Spot( id );
					spot.putFeature( Spot.POSITION_X, x );
					spot.putFeature( Spot.POSITION_Y, y );
					spot.putFeature( Spot.POSITION_Z, z );
					spot.putFeature( Spot.QUALITY, q );
					spot.putFeature( Spot.RADIUS, r );
					spot.setName( name );
				}
				else
				{
					spot = new Spot( x, y, z, r, q, name );
				}
				spot.putFeature( Spot.FRAME, ( double ) t );
				spot.putFeature( Spot.POSITION_T, frameInterval * t );

				if ( importTrack )
				{
					final int trackID = Integer.parseInt( record.get( trackcol ) );
					List< Spot > track = tracks.get( Integer.valueOf( trackID ) );
					if ( null == track )
					{
						track = new ArrayList<>();
						tracks.put( Integer.valueOf( trackID ), track );
					}
					track.add( spot );
				}

				Set< Spot > list = spots.get( Integer.valueOf( t ) );
				if ( null == list )
				{
					list = new HashSet<>();
					spots.put( Integer.valueOf( t ), list );
				}
				list.add( spot );

			}
			catch ( final NumberFormatException nfe )
			{
				nfe.printStackTrace();
				System.out.println( "Could not parse line " + record.getRecordNumber() + ". Malformed number, skipping.\n" + nfe.getMessage() );
				continue;
			}
		}
		logger.log( String.format( "Parsing done. Iterated over %d records.\n", nRecords ) );
		if ( importTrack )
			logger.log( String.format( "Found %d tracks.\n", tracks.size() ) );

		/*
		 * Generate a Model object.
		 */

		final SpotCollection sc = SpotCollection.fromMap( spots );
		sc.setVisible( true );
		logger.log( String.format( "Found %d spots.\n", sc.getNSpots( true ) ) );

		final NavigableSet< Integer > frames = sc.keySet();
		for ( final Integer frame : frames )
			logger.log( String.format( "- frame %4d, n spots = %d\n", frame, sc.getNSpots( frame, true ) ) );

		final Model model = new Model();
		model.setPhysicalUnits( spaceUnits, timeUnits );
		model.setLogger( logger );
		model.setSpots( sc, false );

		if ( importTrack )
		{
			logger.log( "Importing tracks." );
			final Set< Integer > trackIDs = tracks.keySet();
			for ( final Integer trackID : trackIDs )
			{
				final List< Spot > track = tracks.get( trackID );
				track.sort( Spot.frameComparator );
				final Iterator< Spot > it = track.iterator();
				Spot source = it.next();
				while ( it.hasNext() )
				{
					final Spot target = it.next();
					final double weight = source.squareDistanceTo( target );
					model.addEdge( source, target, weight );
					source = target;
				}
			}
			logger.log( " Done.\n" );

		}

		return model;
	}

	public Settings createSettingsFromImp( final ImagePlus imp, final boolean computeAllFeatures )
	{

		logger.log( "Creating settings from opened ImagePlus.\n" );

		/*
		 * Generate a settings object.
		 */

		final Settings settings = new Settings();
		settings.setFrom( imp );
		settings.detectorFactory = new ManualDetectorFactory<>();
		settings.detectorSettings = settings.detectorFactory.getDefaultSettings();
		settings.trackerFactory = new ManualTrackerFactory();
		settings.trackerSettings = settings.trackerFactory.getDefaultSettings();

		if ( computeAllFeatures )
		{

			settings.clearSpotAnalyzerFactories();
			final SpotAnalyzerProvider spotAnalyzerProvider = new SpotAnalyzerProvider();
			final List< String > spotAnalyzerKeys = spotAnalyzerProvider.getKeys();
			for ( final String key : spotAnalyzerKeys )
			{
				final SpotAnalyzerFactory< ? > spotFeatureAnalyzer = spotAnalyzerProvider.getFactory( key );
				settings.addSpotAnalyzerFactory( spotFeatureAnalyzer );
			}

			settings.clearEdgeAnalyzers();
			final EdgeAnalyzerProvider edgeAnalyzerProvider = new EdgeAnalyzerProvider();
			final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
			for ( final String key : edgeAnalyzerKeys )
			{
				final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory( key );
				settings.addEdgeAnalyzer( edgeAnalyzer );
			}

			settings.clearTrackAnalyzers();
			final TrackAnalyzerProvider trackAnalyzerProvider = new TrackAnalyzerProvider();
			final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
			for ( final String key : trackAnalyzerKeys )
			{
				final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory( key );
				settings.addTrackAnalyzer( trackAnalyzer );
			}
		}
		else
		{
			/*
			 * Minimal set of features.
			 */

			// Spot features.
			settings.addSpotAnalyzerFactory( new ManualSpotColorAnalyzerFactory<>() );

			// Edge features.
			settings.addEdgeAnalyzer( new EdgeTargetAnalyzer() );
			settings.addEdgeAnalyzer( new EdgeTimeLocationAnalyzer() );
			settings.addEdgeAnalyzer( new EdgeVelocityAnalyzer() );
			settings.addEdgeAnalyzer( new ManualEdgeColorAnalyzer() );

			// Track features.
			settings.addTrackAnalyzer( new TrackDurationAnalyzer() );
			settings.addTrackAnalyzer( new TrackIndexAnalyzer() );
			settings.addTrackAnalyzer( new TrackLocationAnalyzer() );
			settings.addTrackAnalyzer( new TrackSpeedStatisticsAnalyzer() );
			settings.addTrackAnalyzer( new TrackSpotQualityFeatureAnalyzer() );
		}

		logger.log( "Added the following features to be computed:\n" + settings.toStringFeatureAnalyzersInfo() );
		return settings;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setLogger( final Logger logger )
	{
		this.logger = logger;
	}
}
