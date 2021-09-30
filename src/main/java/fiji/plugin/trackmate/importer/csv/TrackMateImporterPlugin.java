package fiji.plugin.trackmate.importer.csv;

import java.io.File;

import javax.swing.SwingUtilities;

import ij.plugin.PlugIn;

public class TrackMateImporterPlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final ImporterController controller = new ImporterController();
					if ( null != arg && !arg.isEmpty() )
						controller.setCSVFile( new File( arg ) );
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}
}
