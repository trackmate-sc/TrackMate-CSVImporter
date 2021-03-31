package fiji.plugin.trackmate.exporter.csv;

import java.io.File;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class TrackMateExporterPlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final ExporterController controller = new ExporterController();
				if ( null != arg && !arg.isEmpty() )
					controller.setCSVFile( new File( arg ) );
			}
		} );
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		final File csvFile = new File( "samples/data.csv" );
		final File impFile = new File( "samples/171004-4mins-tracking.tif" );
		IJ.openImage( impFile.getAbsolutePath() ).show();

		new TrackMateExporterPlugin().run( csvFile.getAbsolutePath() );
	}
}
