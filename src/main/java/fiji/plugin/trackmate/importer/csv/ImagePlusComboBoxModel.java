package fiji.plugin.trackmate.importer.csv;

import javax.swing.DefaultComboBoxModel;

import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;

public class ImagePlusComboBoxModel extends DefaultComboBoxModel< ImagePlus > implements ImageListener
{
	private static final long serialVersionUID = 1L;

	public ImagePlusComboBoxModel()
	{
		super();
		refresh();
		ImagePlus.addImageListener( this );
	}

	private void refresh()
	{
		final int[] idList = WindowManager.getIDList();
		if ( null == idList )
		{
			removeAllElements();
			return;
		}

		for ( final int id : idList )
		{
			final ImagePlus imp = WindowManager.getImage( id );
			addElement( imp );
		}
	}

	@Override
	public void imageOpened( final ImagePlus imp )
	{
		addElement( imp );
	}

	@Override
	public void imageClosed( final ImagePlus imp )
	{
		removeElement( imp );
	}

	@Override
	public void imageUpdated( final ImagePlus imp )
	{}
}
