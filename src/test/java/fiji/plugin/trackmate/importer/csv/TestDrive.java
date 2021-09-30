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

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final ImporterController controller = new ImporterController();
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
