package fiji.plugin.trackmate.exporter.csv;

import java.io.File;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImageJ;

public class TestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final File csvFile = new File( "samples/test-track.csv" );
		final File impFile = new File( "samples/test-image.tif" );
		IJ.openImage( impFile.getAbsolutePath() ).show();

//		final TrackMateExporterPlugin_ importer = new TrackMateExporterPlugin_();
//		importer.run( csvFile.getAbsolutePath() );

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final ExporterController controller = new ExporterController();
				controller.setCSVFile( csvFile  );
				controller.setXColumn( "Center_of_the_object_0" );
				controller.setYColumn( "Center_of_the_object_1" );
				controller.setZColumn( null );
				controller.setImportTracks( true );
				controller.setRadius( 40 );
			}
		} );
	}
}
