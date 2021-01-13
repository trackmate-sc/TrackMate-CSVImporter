package fiji.plugin.trackmate.importer.roicsv;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import net.imglib2.algorithm.Algorithm;

/**
 * Exports a CSV file to a TrackMate instance and shows it in the TrackMate GUI
 * wizard.
 *
 * @author Jean-Yves Tinevez
 */
public class TrackMateToGUIRoiCsvImporter implements Algorithm
{

	private final String filePath;

	private final ImagePlus imp;

	private String errorMessage;

	private final boolean computeAllFeatures;

	private final Logger logger;

	public TrackMateToGUIRoiCsvImporter( final String filePath, final boolean computeAllFeatures, final ImagePlus imp, final Logger logger )
	{
		this.filePath = filePath;
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
		final TrackMateCsvRoiImporter importer = TrackMateCsvRoiImporter.builder()
				.csvFilePath( filePath )
				.imp( imp )
				.declareAllFeatures( computeAllFeatures )
				.logger( logger )
				.create();

		final String spaceUnit = imp.getCalibration().getUnit();
		final String timeUnit = imp.getCalibration().getTimeUnit();
		final double frameInterval = imp.getCalibration().frameInterval;

		final Model model = importer.getModel( frameInterval, spaceUnit, timeUnit );
		if ( null == model )
		{
			this.errorMessage = importer.getErrorMessage();
			return false;
		}

		final Settings settings = importer.getSettings();
		if ( null == settings )
		{
			this.errorMessage = importer.getErrorMessage();
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
		final DisplaySettings ds = DisplaySettingsIO.readUserDefault();
		final SelectionModel selectionModel = new SelectionModel( model );

		final TrackMateGUIController controller = new TrackMateGUIController( trackmate, ds, selectionModel );
		GuiUtils.positionWindow( controller.getGUI(), settings.imp.getWindow() );
		final String guiState =  "SpotFilter";
		controller.setGUIStateString( guiState );
		final HyperStackDisplayer view = new HyperStackDisplayer( model, controller.getSelectionModel(), settings.imp, ds );
		controller.getGuimodel().addView( view );
		view.render();
		logger.log( "Import complete.\n" );

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
