package fiji.plugin.trackmate.importer.csv;

import java.io.File;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.LoadTrackMatePlugIn_;
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

		new LoadTrackMatePlugIn_().run( targetFile.getAbsolutePath() );


	}

}
