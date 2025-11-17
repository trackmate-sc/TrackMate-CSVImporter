/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2017 - 2025 TrackMate developers.
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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.LoadTrackMatePlugIn;
import ij.ImageJ;

public class CommandLineImportTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		/*
		 * Import the data.
		 */

		final File csvFile = new File( "samples/data.csv" );
		final File impFile = new File( "samples/171004-4mins-tracking.tif" );
		final int frameCol = 0;
		final int xCol = 1;
		final int yCol = 2;
		final int zCol = 3;
		final double radius = 1.;

		final TrackMateImporter importer = TrackMateImporter.builder()
				.csvFilePath( csvFile.getAbsolutePath() )
				.imageFilePath( impFile.getAbsolutePath() )
				.xCol( xCol )
				.yCol( yCol )
				.zCol( zCol )
				.frameCol( frameCol )
				.radius( radius )
				.create();

		final File targetFile = new File("samples/test.xml");
		final boolean ok = importer.saveTo( targetFile );
		if ( !ok )
		{
			System.err.println( importer.getErrorMessage() );
			return;
		}

		/*
		 * Re-open the imported data in TrackMate.
		 */

		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		ImageJ.main( args );

		new LoadTrackMatePlugIn().run( targetFile.getAbsolutePath() );
	}
}
