package fiji.plugin.trackmate.exporter.csv;

import java.io.File;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.importer.roicsv.TrackMateCsvRoiImporterPlugin;
import ij.IJ;
import ij.ImageJ;

public class TestDriveRoisGUI
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final File csvFile = new File( "samples/Table_Segmentation_Output_200512_Stage3.csv" );
		final File impFile = new File( "samples/200512_NeisseriaMeningitidis_WT_iRFP_Stage3.tif" );
		IJ.openImage( impFile.getAbsolutePath() ).show();

		new TrackMateCsvRoiImporterPlugin().run( csvFile.getAbsolutePath() );

	}

}
