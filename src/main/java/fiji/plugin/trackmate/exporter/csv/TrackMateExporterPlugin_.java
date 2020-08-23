package fiji.plugin.trackmate.exporter.csv;

import java.io.File;

import javax.swing.SwingUtilities;

import ij.plugin.PlugIn;

public class TrackMateExporterPlugin_ implements PlugIn
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
					final ExporterController controller = new ExporterController();
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
