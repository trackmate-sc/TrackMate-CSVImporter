package fiji.plugin.trackmate.exporter.csv;

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FRAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_ID_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_NAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_QUALITY_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_RADIUS_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_TRACK_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_COLUMN_NAME;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import net.imglib2.algorithm.Algorithm;

/**
 * Exports a CSV file to a TrackMate instance and shows it in the TrackMate GUI
 * wizard.
 *
 * @author Jean-Yves Tinevez
 */
public class TrackMateToGUIExporter implements Algorithm
{

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd --- HH:mm:ss" );

	private final String filePath;

	private final ImagePlus imp;

	private final Map< String, Integer > fieldMap;

	private String errorMessage;

	private final boolean computeAllFeatures;

	private final Logger logger;

	private final double radius;

	public TrackMateToGUIExporter( final String filePath, final Map< String, Integer > fieldMap, final double radius, final boolean computeAllFeatures, final ImagePlus imp, final Logger logger )
	{
		this.filePath = filePath;
		this.fieldMap = fieldMap;
		this.radius = radius;
		this.computeAllFeatures = computeAllFeatures;
		this.imp = imp;
		this.logger = logger;
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		final Integer noCol = Integer.valueOf( -1 );
		final int xCol = Optional.ofNullable( fieldMap.get( KEY_X_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int yCol = Optional.ofNullable( fieldMap.get( KEY_Y_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int zCol = Optional.ofNullable( fieldMap.get( KEY_Z_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int radiusCol = Optional.ofNullable( fieldMap.get( KEY_RADIUS_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int frameCol = Optional.ofNullable( fieldMap.get( KEY_FRAME_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int qualityCol = Optional.ofNullable( fieldMap.get( KEY_QUALITY_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int idCol = Optional.ofNullable( fieldMap.get( KEY_ID_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int nameCol = Optional.ofNullable( fieldMap.get( KEY_NAME_COLUMN_NAME ) ).orElse( noCol ).intValue();
		final int trackCol = Optional.ofNullable( fieldMap.get( KEY_TRACK_COLUMN_NAME ) ).orElse( noCol ).intValue();

		final TrackMateExporter exporter = TrackMateExporter.builder()
				.csvFilePath( filePath )
				.imp( imp )
				.declareAllFeatures( computeAllFeatures )
				.xCol( xCol )
				.yCol( yCol )
				.zCol( zCol )
				.frameCol( frameCol )
				.idCol( idCol )
				.qualityCol( qualityCol )
				.nameCol( nameCol )
				.trackCol( trackCol )
				.radiusCol( radiusCol )
				.radius( radius )
				.logger( logger )
				.create();

		final String log = "Exporting to TrackMate from CSV file "
				+ filePath + '\n'
				+ "On the " + DATE_FORMAT.format( new Date() ) + '\n'
				+ "By TrackMate CSV Exporter v " + VersionUtils.getVersion( TrackMateToGUIExporter.class ) + '\n';
		logger.log( log + '\n' );

		final String spaceUnit = imp.getCalibration().getUnit();
		final String timeUnit = imp.getCalibration().getTimeUnit();
		final double frameInterval = imp.getCalibration().frameInterval;

		final Model model = exporter.getModel( frameInterval, spaceUnit, timeUnit );
		if ( null == model )
		{
			this.errorMessage = exporter.getErrorMessage();
			return false;
		}

		final Settings settings = exporter.getSettings();
		if ( null == settings )
		{
			this.errorMessage = exporter.getErrorMessage();
			return false;
		}

		/*
		 * Generate a TrackMate object and create TrackMate GUI from it.
		 */

		logger.log( "Computing features.\n" );
		final TrackMate trackmate = new TrackMate( model, settings );
		trackmate.computeSpotFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );
		logger.log( "Done.\n" );

		logger.log( "Launching GUI.\n" );
		final TrackMateGUIController controller = new TrackMateGUIController( trackmate );
		GuiUtils.positionWindow( controller.getGUI(), settings.imp.getWindow() );
		final boolean importTrack = fieldMap.get( KEY_TRACK_COLUMN_NAME ) != null;
		final String guiState = importTrack ? ConfigureViewsDescriptor.KEY : "SpotFilter";
		controller.setGUIStateString( guiState );
		final HyperStackDisplayer view = new HyperStackDisplayer( model, controller.getSelectionModel(), settings.imp );
		controller.getGuimodel().addView( view );
		final Map< String, Object > displaySettings = controller.getGuimodel().getDisplaySettings();
		for ( final String key : displaySettings.keySet() )
			view.setDisplaySettings( key, displaySettings.get( key ) );
		view.render();
		controller.getGUI().getLogger().log( log );
		logger.log( "Export complete.\n" );

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
