package fiji.plugin.trackmate.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
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
import fiji.plugin.trackmate.gui.TrackMateGUIModel;
import fiji.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.providers.EdgeAnalyzerProvider;
import fiji.plugin.trackmate.providers.SpotAnalyzerProvider;
import fiji.plugin.trackmate.providers.TrackAnalyzerProvider;
import fiji.plugin.trackmate.tracking.ManualTrackerFactory;
import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.model.primitives.PositiveInteger;

public class TrackMateExporter
{
	public static final String PLUGIN_VERSION = "0.2.0-SNAPSHOT";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd --- HH:mm:ss" );

	private String errorMessage;

	private final Logger logger;

	private final String csvFilePath;

	private final ImagePlus imp;

	private final String imageFilePath;

	private final int xCol;

	private final int yCol;

	private final int zCol;

	private final int radiusCol;

	private final int frameCol;

	private final int qualityCol;

	private final int idCol;

	private final int nameCol;

	private final int trackCol;

	private final double radius;

	private final boolean declareAllFeatures;

	private TrackMateExporter( final String csvFilePath, final ImagePlus imp, final String imageFilePath, final boolean declareAllFeatures, final int xCol, final int yCol, final int zCol, final int radiusCol, final int frameCol, final int qualityCol, final int idCol, final int nameCol, final int trackCol, final double radius, final Logger logger )
	{
		this.csvFilePath = csvFilePath;
		this.imp = imp;
		this.imageFilePath = imageFilePath;
		this.declareAllFeatures = declareAllFeatures;
		this.xCol = xCol;
		this.yCol = yCol;
		this.zCol = zCol;
		this.radiusCol = radiusCol;
		this.frameCol = frameCol;
		this.qualityCol = qualityCol;
		this.idCol = idCol;
		this.nameCol = nameCol;
		this.trackCol = trackCol;
		this.radius = radius;
		this.logger = logger;
	}

	public boolean exportTo( final File targetFile )
	{
		final StringBuilder errorHolder = new StringBuilder();

		final Settings settings = getSettings();
		if ( null == settings )
			return false;

		String spaceUnits;
		String timeUnits;
		if ( null == imp )
		{
			final String[] units = getUnitsFromImageFile( imageFilePath, errorHolder );
			if ( null == units )
			{
				errorMessage = errorHolder.toString();
				return false;
			}
			spaceUnits = units[ 0 ];
			timeUnits = units[ 1 ];
		}
		else
		{
			spaceUnits = imp.getCalibration().getUnit();
			timeUnits = imp.getCalibration().getTimeUnit();
		}

		final double frameInterval = settings.dt;
		final Model model = getModel( frameInterval, spaceUnits, timeUnits );
		if ( null == model )
			return false;

		final TmXmlWriter writer = new TmXmlWriter( targetFile, logger );

		final String log = "Exported to TrackMate from CSV file "
				+ csvFilePath + '\n'
				+ "On the " + DATE_FORMAT.format( new Date() ) + '\n'
				+ "By TrackMate CSV Exporter v " + PLUGIN_VERSION + '\n';
		writer.appendLog( log );
		writer.appendModel( model );
		writer.appendSettings( settings );
		writer.appendGUIState( new TrackMateGUIModel()
		{
			@Override
			public String getGUIStateString()
			{
				final String guiState = trackCol >= 0 ? ConfigureViewsDescriptor.KEY : "SpotFilter";
				return guiState;
			}
		} );

		try
		{
			writer.writeToFile();
			logger.log( "Data saved to: " + targetFile.toString() + '\n' );
		}
		catch ( final FileNotFoundException e )
		{
			final String str = "File not found:\n" + e.getMessage() + '\n';
			logger.error( str );
			errorMessage = str;

			return false;
		}
		catch ( final IOException e )
		{
			final String str = "Input/Output error:\n" + e.getMessage() + '\n';
			logger.error( str );
			errorMessage = str;
			return false;
		}
		return true;
	}

	public Settings getSettings()
	{
		final StringBuilder errorHolder = new StringBuilder();
		final Settings settings = ( imp == null )
				? createSettingsFromImageFile( imageFilePath, errorHolder, logger )
				: createSettingsFromImp( imp, logger );
		settings.detectorFactory = new ManualDetectorFactory<>();
		settings.detectorSettings = settings.detectorFactory.getDefaultSettings();
		settings.trackerFactory = new ManualTrackerFactory();
		settings.trackerSettings = settings.trackerFactory.getDefaultSettings();

		if ( declareAllFeatures )
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

	public Model getModel( final double frameInterval, final String spaceUnit, final String timeUnit )
	{
		@SuppressWarnings( "unused" )
		CSVMetadata csvMetadata = null;
		try
		{
			csvMetadata = CSVMetadata.parse( csvFilePath );
		}
		catch ( final IOException e )
		{
			errorMessage = "problem parsing CSV metadata:\n" + e.getMessage();
			e.printStackTrace();
			return null;
		}

		/*
		 * Prepare spot & track Collections.
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
			in = new FileReader( csvFilePath );
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
			errorMessage = "Problem accessing file " + csvFilePath + ":\n" + e.getMessage();
			return null;
		}

		/*
		 * Flags.
		 */

		final boolean importZ = zCol >= 0;
		final boolean importQuality = qualityCol >= 0;
		final boolean importRadius = radiusCol >= 0;
		final boolean importName = nameCol >= 0;
		final boolean importID = idCol >= 0;
		final boolean importTrack = trackCol >= 0;


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
				final double x = Double.parseDouble( record.get( xCol ) );
				final double y = Double.parseDouble( record.get( yCol ) );
				final double z = ( importZ ) ? Double.parseDouble( record.get( zCol ) ) : 0.;

				// 1-based to 0-based.
				final int t = Integer.parseInt( record.get( frameCol ) );

				double q = 1.;
				if ( importQuality )
					q = Double.parseDouble( record.get( qualityCol ) );

				double r = radius;
				if ( importRadius )
					r = Double.parseDouble( record.get( radiusCol ) );

				String name = null;
				if ( importName )
					name = record.get( nameCol );

				final Spot spot;
				if ( importID )
				{
					// Hijack spot IDs: we force ID to match ID provided.
					final int id = Integer.parseInt( record.get( idCol ) );
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
					final int trackID = Integer.parseInt( record.get( trackCol ) );
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
				logger.log( " Could not parse line " + record.getRecordNumber() + ". Malformed number, skipping.\n" + nfe.getMessage() );
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
		model.setPhysicalUnits( spaceUnit, timeUnit );
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

	private static Settings createSettingsFromImageFile( final String imageFile, final StringBuilder errorHolder, final Logger logger )
	{
		return createSettingsFromImageFile( imageFile, 0, errorHolder, logger );
	}

	private static Settings createSettingsFromImageFile( final String imageFile, final int series, final StringBuilder errorHolder, final Logger logger )
	{
		logger.log( "Creating settings from image file.\n" );
		final Settings settings = new Settings();
		try
		{
			DebugTools.setRootLevel( "ERROR" );
			final ImporterOptions options = new ImporterOptions();
			options.setId( imageFile );
			options.setQuiet( true );
			options.setWindowless( true );
			final ImportProcess process = new ImportProcess( options );
			if ( !process.execute() )
			{
				errorHolder.append( "Error while preparing the import of metadata." );
				return null;
			}

			final IMetadata metadata = process.getOMEMetadata();
			final Length pixelsPhysicalSizeX = metadata.getPixelsPhysicalSizeX( series );
			final Length pixelsPhysicalSizeY = metadata.getPixelsPhysicalSizeY( series );
			final Length pixelsPhysicalSizeZ = metadata.getPixelsPhysicalSizeZ( series );
			final Time timeIncrement = metadata.getPixelsTimeIncrement( series );
			final PositiveInteger sizeX = metadata.getPixelsSizeX( series );
			final PositiveInteger sizeY = metadata.getPixelsSizeY( series );
			final PositiveInteger sizeZ = metadata.getPixelsSizeZ( series );
			final PositiveInteger sizeT = metadata.getPixelsSizeT( series );

			settings.width = sizeX.getValue().intValue();
			settings.height = sizeY.getValue().intValue();
			settings.nslices = sizeZ.getValue().intValue();
			settings.nframes = sizeT.getValue().intValue();
			settings.dx = pixelsPhysicalSizeX.value().doubleValue();
			settings.dy = pixelsPhysicalSizeY.value().doubleValue();
			settings.dz = Optional.ofNullable( pixelsPhysicalSizeZ )
					.orElse( new Length( Double.valueOf( 1. ), UNITS.PIXEL ) )
					.value().doubleValue();
			settings.dt = timeIncrement.value().doubleValue();
			settings.xstart = 0;
			settings.xend = settings.width - 1;
			settings.ystart = 0;
			settings.yend = settings.height - 1;
			settings.zstart = 0;
			settings.zend = settings.nslices - 1;
			final File file = new File( imageFile );
			settings.imageFileName = file.getName();
			settings.imageFolder = file.getParent();
		}
		catch ( final IOException | FormatException e )
		{
			errorHolder.append( "Problem reading metadata:\n" + e.getMessage() );
			return null;
		}
		return settings;
	}

	private static String[] getUnitsFromImageFile( final String imageFilePath, final StringBuilder errorHolder )
	{
		return getUnitsFromImageFile( imageFilePath, 0, errorHolder );
	}

	private static String[] getUnitsFromImageFile( final String imageFilePath, final int series, final StringBuilder errorHolder )
	{
		try
		{
			DebugTools.setRootLevel( "ERROR" );
			final ImporterOptions options = new ImporterOptions();
			options.setId( imageFilePath );
			options.setQuiet( true );
			options.setWindowless( true );
			final ImportProcess process = new ImportProcess( options );
			if ( !process.execute() )
			{
				errorHolder.append( "Error while preparing the import of metadata." );
				return null;
			}

			final IMetadata metadata = process.getOMEMetadata();
			final String spaceUnit = metadata.getPixelsPhysicalSizeX( series ).unit().getSymbol();
			final String timeUnit = metadata.getPixelsTimeIncrement( series ).unit().getSymbol();
			return new String[] { spaceUnit, timeUnit };
		}
		catch ( final IOException | FormatException e )
		{
			errorHolder.append( "Problem reading metadata:\n" + e.getMessage() );
			return null;
		}
	}

	private static Settings createSettingsFromImp( final ImagePlus imp, final Logger logger )
	{
		logger.log( "Creating settings from opened ImagePlus.\n" );
		final Settings settings = new Settings();
		settings.setFrom( imp );
		return settings;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static final class Builder
	{

		private static class Values
		{
			private String csvFilePath;

			private String imageFilePath;

			private ImagePlus imp;

			private int xCol = -1;

			private int yCol = -1;

			private int zCol = -1;

			private int frameCol = -1;

			private int idCol = -1;

			private int qualityCol = -1;

			private int nameCol = -1;

			private int trackCol = -1;

			private int radiusCol = -1;

			private double radius = 1.;

			private Logger logger = Logger.DEFAULT_LOGGER;

			private boolean declareAllFeatures = true;
		}

		private final Values values;

		private Builder()
		{
			this.values = new Values();
		}

		public Builder csvFilePath( final String csvFilePath )
		{
			values.csvFilePath = csvFilePath;
			return this;
		}

		public Builder imageFilePath( final String imageFilePath )
		{
			values.imageFilePath = imageFilePath;
			return this;
		}

		public Builder imp( final ImagePlus imp )
		{
			values.imp = imp;
			return this;
		}

		public Builder declareAllFeatures( final boolean declareAllFeatures )
		{
			values.declareAllFeatures = declareAllFeatures;
			return this;
		}

		public Builder xCol( final int xCol )
		{
			values.xCol = xCol;
			return this;
		}

		public Builder yCol( final int yCol )
		{
			values.yCol = yCol;
			return this;
		}

		public Builder zCol( final int zCol )
		{
			values.zCol = zCol;
			return this;
		}

		public Builder frameCol( final int frameCol )
		{
			values.frameCol = frameCol;
			return this;
		}

		public Builder idCol( final int idCol )
		{
			values.idCol = idCol;
			return this;
		}

		public Builder qualityCol( final int qualityCol )
		{
			values.qualityCol = qualityCol;
			return this;
		}

		public Builder trackCol( final int trackCol )
		{
			values.trackCol = trackCol;
			return this;
		}

		public Builder nameCol( final int nameCol )
		{
			values.nameCol = nameCol;
			return this;
		}

		public Builder radiusCol( final int radiusCol )
		{
			values.radiusCol = radiusCol;
			return this;
		}

		public Builder radius( final double radius )
		{
			values.radius = radius;
			return this;
		}

		public Builder logger( final Logger logger )
		{
			values.logger = logger;
			return this;
		}

		public TrackMateExporter create()
		{
			return new TrackMateExporter(
					values.csvFilePath,
					values.imp,
					values.imageFilePath,
					values.declareAllFeatures,
					values.xCol,
					values.yCol,
					values.zCol,
					values.radiusCol,
					values.frameCol,
					values.qualityCol,
					values.idCol,
					values.nameCol,
					values.trackCol,
					values.radius,
					values.logger );
		}

	}
}
