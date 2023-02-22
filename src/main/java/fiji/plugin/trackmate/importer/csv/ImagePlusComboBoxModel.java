/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2017 - 2023 TrackMate developers.
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
