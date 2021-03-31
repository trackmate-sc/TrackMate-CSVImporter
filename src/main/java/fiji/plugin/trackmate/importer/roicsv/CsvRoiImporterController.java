package fiji.plugin.trackmate.importer.roicsv;

import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import fiji.plugin.trackmate.exporter.csv.CSVMetadata;
import ij.ImagePlus;
import ij.io.FileInfo;

public class CsvRoiImporterController
{
	private final CsvRoiImporterPanel view;

	private static File file;

	private boolean imageOk;

	private boolean csvOk;

	private CSVMetadata csvMetadata;

	public CsvRoiImporterController()
	{
		this.view = new CsvRoiImporterPanel();
		view.btnBrowse.addActionListener( ( e ) -> browse() );
		view.textFieldFile.addActionListener( ( e ) -> setCSVFile( new File( view.textFieldFile.getText() ) ) );
		view.btnImport.addActionListener( ( e ) -> doImport() );
		view.comboBoxImp.addActionListener( ( e ) -> checkImage() );
		checkImage();
		final JFrame frame = new JFrame( "TrackMate CSV importer" );
		frame.setIconImage( CsvRoiImporterPanel.ICON.getImage() );
		frame.getContentPane().add( view );
		frame.pack();
		frame.setVisible( true );
	}

	private void doImport()
	{
		view.btnImport.setEnabled( false );
		new Thread( "TrackMate CSV ROI importer thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final String filePath = view.textFieldFile.getText();
					final ImagePlus imp = ( ImagePlus ) view.comboBoxImp.getSelectedItem();

					final TrackMateToGUIRoiCsvImporter exporter = new TrackMateToGUIRoiCsvImporter(
							filePath,
							view.chckbxComputeFeatures.isSelected(),
							imp,
							view.getLogger() );
					if ( !exporter.checkInput() || !exporter.process() )
					{
						error( "Error importing CSV file:\n" + exporter.getErrorMessage() );
						return;
					}
					log( "Export successful.\n" );
				}
				finally
				{
					view.btnImport.setEnabled( true );
				}
			}
		}.start();
	}

	private final boolean checkImage()
	{
		imageOk = false;
		final int i = view.comboBoxImp.getSelectedIndex();
		if ( i < 0 )
		{
			error( "Please open and select an image.\n" );
			view.btnImport.setEnabled( false );
			return false;
		}
		final ImagePlus imp = view.comboBoxImp.getItemAt( i );
		if ( null == imp )
		{
			error( "Please open and select an image.\n" );
			view.btnImport.setEnabled( false );
			return false;
		}

		log( "Selected image " + imp + '\n' );
		final FileInfo fileInfo = imp.getOriginalFileInfo();
		if ( null == fileInfo )
			log( "Could not find a saved file for this image. The generated TrackMate will not reload properly.\n" );

		imageOk = true;
		if ( csvOk )
			view.btnImport.setEnabled( true );

		return true;
	}

	private void browse()
	{
		final File file = askForCSVfile();
		if ( null == file )
			return;

		setCSVFile( file );
	}

	private File askForCSVfile()
	{
		if ( null == file )
			CsvRoiImporterController.file = new File( System.getProperty( "user.home" ) );

		final FileDialog dialog = new FileDialog( new JFrame(), "Open a CSV file", FileDialog.LOAD );
		dialog.setIconImage( TRACKMATE_ICON.getImage() );
		dialog.setDirectory( file.getAbsolutePath() );
		dialog.setFile( file.getName() );
		dialog.setVisible( true );
		String selectedFile = dialog.getFile();
		if ( null == selectedFile ) { return null; }
		if ( !selectedFile.endsWith( ".csv" ) )
			selectedFile += ".csv";
		file = new File( dialog.getDirectory(), selectedFile );
		return file;
	}

	void setCSVFile( final File file )
	{
		csvOk = false;
		CsvRoiImporterController.file = file;
		view.textFieldFile.setText( file.getAbsolutePath() );
		log( "Inspecting CSV file: " + file + '\n' );

		view.btnImport.setEnabled( false );
		if ( checkCSV() )
		{
			csvOk = true;
			if ( imageOk )
				view.btnImport.setEnabled( true );
		}
	}

	private boolean checkCSV()
	{
		final String filePath = view.textFieldFile.getText();
		csvMetadata = null;
		try
		{
			csvMetadata = CSVMetadata.parse( filePath );
			log( "Metadata from CSV file:\n" );
			log( csvMetadata.toString() + '\n' );
		}
		catch ( final FileNotFoundException e )
		{
			error( "Could not find CSV file:\n" + e.getMessage() + '\n' );
			e.printStackTrace();
			return false;
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
			error( "Could not browse CSV file:\n" + e.getMessage() + "\n" );
			return false;
		}
		return true;
	}

	private void error( final String string )
	{
		view.getLogger().error( string );
	}

	private void log( final String string )
	{
		view.getLogger().log( string );
	}
}
