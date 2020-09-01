package fiji.plugin.trackmate.importer.csv;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ij.ImagePlus;

public class ImagePlusListRenderer implements ListCellRenderer< ImagePlus >
{

	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent( final JList< ? extends ImagePlus > list, final ImagePlus imp, final int index, final boolean isSelected, final boolean hasFocus )
	{
		if ( null != imp )
			DEFAULT_RENDERER.setText( imp.getTitle() );
		else
			DEFAULT_RENDERER.setText( "" );
		return DEFAULT_RENDERER;
	}

}
