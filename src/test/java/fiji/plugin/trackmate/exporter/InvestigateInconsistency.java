package fiji.plugin.trackmate.exporter;

import java.io.File;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImageJ;

public class InvestigateInconsistency
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final File csvFile = new File( "samples/180319_csv_inconsistency/171205-1_crop.csv" );
		final File impFile = new File( "samples/180319_csv_inconsistency/fakeImages.tif" );
		IJ.openImage( impFile.getAbsolutePath() ).show();

		new TrackMateExporterPlugin_().run( csvFile.getAbsolutePath() );
	}
}
