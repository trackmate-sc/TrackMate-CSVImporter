package fiji.plugin.trackmate.exporter;

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FRAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_ID_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_NAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_QUALITY_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_TRACK_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_COLUMN_NAME;
import static fiji.plugin.trackmate.gui.TrackMateWizard.TRACKMATE_ICON;

import java.awt.Color;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import ij.ImagePlus;
import ij.io.FileInfo;

public class ExporterController
{
	private static final String NONE_COLUMN = "Don't use";

	private final ExporterPanel view;

	private File file;

	private boolean imageOk;

	private boolean csvOk;

	private CSVMetadata csvMetadata;

	public ExporterController()
	{
		this.view = new ExporterPanel();
		view.btnBrowse.addActionListener( ( e ) -> browse() );
		view.textFieldFile.addActionListener( ( e ) -> setCSVFile( new File( view.textFieldFile.getText() ) ) );
		view.btnExport.addActionListener( ( e ) -> export() );
		view.comboBoxImp.addActionListener( ( e ) -> checkImage() );
		checkImage();
		final JFrame frame = new JFrame( "TrackMate CSV importer" );
		frame.setIconImage( ExporterPanel.ICON.getImage() );
		frame.getContentPane().add( view );
		frame.setSize( 600, 600 );
		frame.setVisible( true );
	}

	private void export()
	{
		final String filePath = view.textFieldFile.getText();

		final Map< String, Integer > fieldMap = new HashMap<>();
		fieldMap.put( KEY_X_COLUMN_NAME, Integer.valueOf( view.comboBoxXCol.getSelectedIndex() ) );
		fieldMap.put( KEY_Y_COLUMN_NAME, Integer.valueOf( view.comboBoxYCol.getSelectedIndex() ) );
		fieldMap.put( KEY_Z_COLUMN_NAME, Integer.valueOf( view.comboBoxZCol.getSelectedIndex() ) );
		fieldMap.put( KEY_FRAME_COLUMN_NAME, Integer.valueOf( view.comboBoxFrameCol.getSelectedIndex() ) );

		if ( view.chckbxImportTracks.isSelected() && view.comboBoxTrackCol.getSelectedItem() != NONE_COLUMN )
			fieldMap.put( KEY_TRACK_COLUMN_NAME, Integer.valueOf( view.comboBoxTrackCol.getSelectedIndex() ) );

		if ( view.comboBoxQualityCol.getSelectedItem() != NONE_COLUMN )
			fieldMap.put( KEY_QUALITY_COLUMN_NAME, Integer.valueOf( view.comboBoxQualityCol.getSelectedIndex() ) );

		if ( view.comboBoxNameCol.getSelectedItem() != NONE_COLUMN )
			fieldMap.put( KEY_NAME_COLUMN_NAME, Integer.valueOf( view.comboBoxNameCol.getSelectedIndex() ) );

		if ( view.comboBoxIDCol.getSelectedItem() != NONE_COLUMN )
			fieldMap.put( KEY_ID_COLUMN_NAME, Integer.valueOf( view.comboBoxIDCol.getSelectedIndex() ) );

		final ImagePlus imp = ( ImagePlus ) view.comboBoxImp.getSelectedItem();
		final double radius = ( ( Number ) view.ftfRadius.getValue() ).doubleValue();
		final TrackMateExporter exporter = new TrackMateExporter( filePath, fieldMap, radius, imp );
		if ( !exporter.checkInput() || !exporter.process() )
		{
			error( "Error importing CSV file:\n" + exporter.getErrorMessage() );
			return;
		}
		log( "Export successful.\n" );
	}

	private final boolean checkImage()
	{
		imageOk = false;
		final int i = view.comboBoxImp.getSelectedIndex();
		if ( i < 0 )
		{
			error( "Please open and select an image.\n" );
			view.btnExport.setEnabled( false );
			return false;
		}
		final ImagePlus imp = view.comboBoxImp.getItemAt( i );
		if ( null == imp )
		{
			error( "Please open and select an image.\n" );
			view.btnExport.setEnabled( false );
			return false;
		}

		log( "Selected image " + imp + '\n' );
		final FileInfo fileInfo = imp.getOriginalFileInfo();
		if ( null == fileInfo )
			log( "Could not find a saved file for this image. The generated TrackMate will not reload properly.\n" );

		/*
		 * Tentative automatic radius.
		 */
		view.labelRadiusUnit.setText( imp.getCalibration().getUnit() );
		final double r = 2.5 * imp.getCalibration().pixelWidth;
		view.ftfRadius.setValue( Double.valueOf( r ) );

		imageOk = true;
		if ( csvOk )
			view.btnExport.setEnabled( true );

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
			this.file = new File( System.getProperty( "user.home" ) );

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
		this.file = file;
		view.textFieldFile.setText( file.getAbsolutePath() );
		log( "Inspecting CSV file: " + file + '\n' );

		view.btnExport.setEnabled( false );
		if ( readHeaders() && readMetadata() )
		{
			csvOk = true;
			if ( imageOk )
				view.btnExport.setEnabled( true );
		}
	}

	private boolean readMetadata()
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

	private boolean readHeaders()
	{
		final String filePath = view.textFieldFile.getText();

		/*
		 * Open and parse file.
		 */

		Reader in;
		CSVParser records;
		try
		{
			in = new FileReader( filePath );
		}
		catch ( final FileNotFoundException e )
		{
			error( "Could not find CSV file:\n" + e.getMessage() + '\n' );
			e.printStackTrace();
			clearComboBoxes();
			return false;
		}

		try
		{
			final CSVFormat csvFormat = CSVFormat.EXCEL
					.withHeader()
					.withCommentMarker( '#' );
			records = csvFormat.parse( in );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			error( "Could not browse CSV file:\n" + e.getMessage() + "\n" );
			clearComboBoxes();
			return false;
		}

		final Map< String, Integer > headerMap = records.getHeaderMap();

		// Iterate in column orders.
		final ArrayList< String > headers = new ArrayList<>( headerMap.keySet() );

		if ( headers.isEmpty() )
		{
			error( "Could not read the header of the CSV file.\nIt does not seem present.\n" );
			return false;
		}

		final String[] mandatory = headers.toArray( new String[] {} );
		view.comboBoxXCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		view.comboBoxYCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		view.comboBoxZCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		view.comboBoxFrameCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		view.comboBoxTrackCol.setModel( new DefaultComboBoxModel<>( mandatory ) );

		// Try to be clever and guess from header names.
		int tcol = -1;
		int xcol = -1;
		int ycol = -1;
		int zcol = -1;
		int trackcol = -1;
		for ( int i = 0; i < mandatory.length; i++ )
		{
			final String current = mandatory[ i ];

			if ( current.toLowerCase().startsWith( "x" ) )
			{
				if ( xcol < 0 || ( current.length() < mandatory[ xcol ].length() ) )
					xcol = i;
			}

			if ( current.toLowerCase().startsWith( "y" ) )
			{
				if ( ycol < 0 || ( current.length() < mandatory[ ycol ].length() ) )
					ycol = i;
			}

			if ( current.toLowerCase().startsWith( "z" ) )
			{
				if ( zcol < 0 || ( current.length() < mandatory[ zcol ].length() ) )
					zcol = i;
			}

			if ( current.toLowerCase().startsWith( "frame" )
					|| current.toLowerCase().startsWith( "time" )
					|| current.toLowerCase().startsWith( "t" ) )
			{
				if ( tcol < 0 || current.equals( "frame" ) )
					tcol = i;
			}

			if ( current.toLowerCase().startsWith( "track" ) )
			{
				if ( trackcol < 0 || current.equals( "track" ) )
					trackcol = i;
			}
		}
		if ( tcol < 0 )
			tcol = 0 % ( mandatory.length - 1 );
		if ( xcol < 0 )
			xcol = 1 % ( mandatory.length - 1 );
		if ( ycol < 0 )
			ycol = 2 % ( mandatory.length - 1 );
		if ( zcol < 0 )
			zcol = 3 % ( mandatory.length - 1 );
		if ( trackcol < 0 )
			trackcol = 4 % ( mandatory.length - 1 );

		view.comboBoxXCol.setSelectedIndex( xcol );
		view.comboBoxYCol.setSelectedIndex( ycol );
		view.comboBoxZCol.setSelectedIndex( zcol );
		view.comboBoxFrameCol.setSelectedIndex( tcol );
		view.comboBoxTrackCol.setSelectedIndex( trackcol );

		// Add a NONE for non mandatory columns
		headers.add( NONE_COLUMN );
		final String[] nonMandatory = headers.toArray( new String[] {} );
		view.comboBoxQualityCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		view.comboBoxNameCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		view.comboBoxIDCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );

		int idcol = headers.indexOf( NONE_COLUMN );
		int qualitycol = headers.indexOf( NONE_COLUMN );
		int namecol = headers.indexOf( NONE_COLUMN );
		for ( int i = 0; i < nonMandatory.length; i++ )
		{
			final String current = nonMandatory[ i ];

			if ( current.toLowerCase().startsWith( "id" ) )
				idcol = i;

			if ( current.toLowerCase().startsWith( "name" ) )
				namecol = i;

			if ( current.toLowerCase().startsWith( "q" ) )
				qualitycol = i;
		}

		view.comboBoxIDCol.setSelectedIndex( idcol );
		view.comboBoxQualityCol.setSelectedIndex( qualitycol );
		view.comboBoxNameCol.setSelectedIndex( namecol );
		try
		{
			in.close();
		}
		catch ( final IOException e )
		{
			error( "Problem closing the CSV file:\n" + e.getMessage() + '\n' );
			e.printStackTrace();
		}
		return true;
	}

	private void clearComboBoxes()
	{
		final ArrayList< JComboBox< String > > comboBoxes = new ArrayList<>();
		comboBoxes.add( view.comboBoxXCol );
		comboBoxes.add( view.comboBoxYCol );
		comboBoxes.add( view.comboBoxZCol );
		comboBoxes.add( view.comboBoxFrameCol );
		comboBoxes.add( view.comboBoxQualityCol );
		comboBoxes.add( view.comboBoxNameCol );
		comboBoxes.add( view.comboBoxIDCol );
		comboBoxes.add( view.comboBoxTrackCol );
		for ( final JComboBox< String > cb : comboBoxes )
			cb.setModel( new DefaultComboBoxModel<>() );
	}

	private void error( final String string )
	{
		view.log( string, Color.RED.darker() );
	}

	private void log( final String string )
	{
		view.log( string, Color.BLACK );
	}

}
