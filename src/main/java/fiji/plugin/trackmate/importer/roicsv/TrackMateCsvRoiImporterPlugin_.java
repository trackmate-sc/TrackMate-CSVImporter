package fiji.plugin.trackmate.importer.roicsv;

import java.io.File;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.ImageJ;
import ij.plugin.PlugIn;

public class TrackMateCsvRoiImporterPlugin_ implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final CsvRoiImporterController controller = new CsvRoiImporterController();
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

		new TrackMateCsvRoiImporterPlugin_().run( "" );
	}
}
