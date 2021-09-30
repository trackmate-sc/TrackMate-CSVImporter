/*-
 * #%L
 * A CSV importer for TrackMate.
 * %%
 * Copyright (C) 2017 - 2021 Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
