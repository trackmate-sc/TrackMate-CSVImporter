package fiji.plugin.trackmate.exporter;

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_TRACK_COLUMN_NAME;

import java.util.Map;

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

	public static final String PLUGIN_VERSION = "0.1.0-SNAPSHOT";

	private final String filePath;

	private final ImagePlus imp;

	private final Map< String, Integer > fieldMap;

	private String errorMessage;

	private final boolean computeAllFeatures;

	private Logger logger = Logger.VOID_LOGGER;

	private final double radius;

	private final TrackMateExporter exporter;

	public TrackMateToGUIExporter( final String filePath, final Map< String, Integer > fieldMap, final double radius, final boolean computeAllFeatures, final ImagePlus imp )
	{
		this.filePath = filePath;
		this.fieldMap = fieldMap;
		this.radius = radius;
		this.computeAllFeatures = computeAllFeatures;
		this.imp = imp;
		this.exporter = new TrackMateExporter();
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		final String spaceUnit = imp.getCalibration().getUnit();
		final String timeUnit = imp.getCalibration().getTimeUnit();
		final double frameInterval = imp.getCalibration().frameInterval;

		final Model model = exporter.importModel( filePath, fieldMap, radius, spaceUnit, timeUnit, frameInterval );
		if ( null == model )
		{
			this.errorMessage = exporter.getErrorMessage();
			return false;
		}

		final Settings settings = exporter.createSettingsFromImp( imp, computeAllFeatures );
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
		logger.log( "Export complete.\n" );

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setLogger( final Logger logger )
	{
		this.logger = logger;
		exporter.setLogger( logger );
	}
}
