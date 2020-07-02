package fiji.plugin.trackmate.exporter.csv;

import java.io.File;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.importer.roicsv.TrackMateToGUIRoiCsvImporter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class TestDriveRois
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final File csvFile = new File( "samples/Table_Segmentation_Output_200512_Stage3.csv" );
		final File impFile = new File( "samples/200512_NeisseriaMeningitidis_WT_iRFP_Stage3.tif" );
		final ImagePlus imp = IJ.openImage( impFile.getAbsolutePath() );
		imp.show();

		final TrackMateToGUIRoiCsvImporter exporter = new TrackMateToGUIRoiCsvImporter(
				csvFile.getAbsolutePath(),
				true,
				imp,
				Logger.DEFAULT_LOGGER );
		if ( !exporter.checkInput() || !exporter.process() )
		{
			System.err.println( "Error importing CSV file:\n" + exporter.getErrorMessage() );
			return;
		}
		System.out.println( "Export successful.\n" );

	}

}
