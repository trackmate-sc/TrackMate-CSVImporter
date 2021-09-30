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

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FRAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_ID_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_NAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_QUALITY_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_TRACK_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_COLUMN_NAME;
import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import ij.ImagePlus;
import ij.io.FileInfo;

public class ImporterController
{
	private static final String NONE_COLUMN = "Don't use";

	private final ImporterPanel view;

	private File file;

	private boolean imageOk;

	private boolean csvOk;

	private CSVMetadata csvMetadata;

	private Map< String, Integer > headerMap;

	public ImporterController()
	{
		this.view = new ImporterPanel();
		view.btnBrowse.addActionListener( ( e ) -> browse() );
		view.textFieldFile.addActionListener( ( e ) -> setCSVFile( new File( view.textFieldFile.getText() ) ) );
		view.btnImport.addActionListener( ( e ) -> doImport() );
		view.comboBoxImp.addActionListener( ( e ) -> checkImage() );
		checkImage();
		final JFrame frame = new JFrame( "TrackMate CSV importer" );
		frame.setIconImage( ImporterPanel.ICON.getImage() );
		frame.getContentPane().add( view );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	public void setXColumn( final String xColumnName )
	{
		view.comboBoxXCol.setSelectedItem( xColumnName );
	}

	public void setYColumn( final String yColumnName )
	{
		view.comboBoxYCol.setSelectedItem(  yColumnName );
	}

	public void setZColumn( final String zColumnName )
	{
		view.comboBoxZCol.setSelectedItem( ( zColumnName == null ) ? NONE_COLUMN : zColumnName );
	}

	public void setTrackColumn( final String trackColumnName )
	{
		view.comboBoxTrackCol.setSelectedItem(  trackColumnName );
	}

	public void setIDColumn( final String idColumnName )
	{
		view.comboBoxIDCol.setSelectedItem( ( idColumnName == null ) ? NONE_COLUMN : idColumnName );
	}

	public void setNameColumn( final String nameColumnName )
	{
		view.comboBoxNameCol.setSelectedItem( ( nameColumnName == null ) ? NONE_COLUMN : nameColumnName );
	}

	public void setQualityColumn( final String qualityColumnName )
	{
		view.comboBoxQualityCol.setSelectedItem( ( qualityColumnName == null ) ? NONE_COLUMN : qualityColumnName );
	}

	public void setFrameColumn( final String frameColumnName )
	{
		view.comboBoxFrameCol.setSelectedItem( frameColumnName );
	}

	public void setImage( final ImagePlus imp)
	{
		view.comboBoxImp.setSelectedItem( imp );
		checkImage();
	}

	public void setRadius( final double radius )
	{
		view.ftfRadius.setValue( Double.valueOf( radius ) );
	}

	public void setImportTracks( final boolean doImportTracks )
	{
		final boolean selected = view.chckbxImportTracks.isSelected();
		if ( selected != doImportTracks)
			view.chckbxImportTracks.doClick();
	}

	public void setComputeFeatures( final boolean doComputeFeatures)
	{
		view.chckbxComputeFeatures.setSelected( doComputeFeatures );
	}

	private void doImport()
	{
		view.btnImport.setEnabled( false );
		new Thread( "TrackMate CSV importer thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final String filePath = view.textFieldFile.getText();

					final Map< String, Integer > fieldMap = new HashMap<>();

					fieldMap.put( KEY_X_COLUMN_NAME, headerMap.get( view.comboBoxXCol.getSelectedItem() ) );
					fieldMap.put( KEY_Y_COLUMN_NAME, headerMap.get( view.comboBoxYCol.getSelectedItem() ) );
					fieldMap.put( KEY_FRAME_COLUMN_NAME, headerMap.get( view.comboBoxFrameCol.getSelectedItem() ) );

					final Integer zCol = headerMap.get( view.comboBoxZCol.getSelectedItem() );
					if ( view.comboBoxZCol.getSelectedItem() != NONE_COLUMN && null != zCol )
						fieldMap.put( KEY_Z_COLUMN_NAME, zCol );

					final Integer trackCol = headerMap.get( view.comboBoxTrackCol.getSelectedItem() );
					if ( view.chckbxImportTracks.isSelected() && view.comboBoxTrackCol.getSelectedItem() != NONE_COLUMN && null != trackCol )
						fieldMap.put( KEY_TRACK_COLUMN_NAME, trackCol );

					final Integer qualityCol = headerMap.get( view.comboBoxQualityCol.getSelectedItem() );
					if ( view.comboBoxQualityCol.getSelectedItem() != NONE_COLUMN && null != qualityCol )
						fieldMap.put( KEY_QUALITY_COLUMN_NAME, qualityCol );

					final Integer nameCol = headerMap.get( view.comboBoxNameCol.getSelectedItem() );
					if ( view.comboBoxNameCol.getSelectedItem() != NONE_COLUMN && null != nameCol )
						fieldMap.put( KEY_NAME_COLUMN_NAME, nameCol );

					final Integer idCol = headerMap.get( view.comboBoxIDCol.getSelectedItem() );
					if ( view.comboBoxIDCol.getSelectedItem() != NONE_COLUMN && null != idCol )
						fieldMap.put( KEY_ID_COLUMN_NAME, idCol );

					final ImagePlus imp = ( ImagePlus ) view.comboBoxImp.getSelectedItem();
					final double radius = ( ( Number ) view.ftfRadius.getValue() ).doubleValue();
					final TrackMateToGUIImporter importer = new TrackMateToGUIImporter( filePath, fieldMap, radius, view.chckbxComputeFeatures.isSelected(), imp, view.getLogger() );
					if ( !importer.checkInput() || !importer.process() )
					{
						error( "Error importing CSV file:\n" + importer.getErrorMessage() );
						return;
					}
					log( "Import successful.\n" );
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

		/*
		 * Tentative automatic radius.
		 */
		view.labelRadiusUnit.setText( imp.getCalibration().getUnit() );
		final double r = 2.5 * imp.getCalibration().pixelWidth;
		view.ftfRadius.setValue( Double.valueOf( r ) );

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
			this.file = new File( System.getProperty( "user.home" ) );

		final FileDialog dialog = new FileDialog( new JFrame(), "Open a CSV file", FileDialog.LOAD );
		dialog.setIconImage( TRACKMATE_ICON.getImage() );
		dialog.setDirectory( file.getAbsolutePath() );
		dialog.setFile( file.getName() );
		dialog.setVisible( true );
		String selectedFile = dialog.getFile();
		if ( null == selectedFile )
			return null;

		if ( !selectedFile.endsWith( ".csv" ) )
			selectedFile += ".csv";

		file = new File( dialog.getDirectory(), selectedFile );
		return file;
	}

	public void setCSVFile( final File file )
	{
		csvOk = false;
		this.file = file;
		view.textFieldFile.setText( file.getAbsolutePath() );
		log( "Inspecting CSV file: " + file + '\n' );

		view.btnImport.setEnabled( false );
		if ( readHeaders() && readMetadata() )
		{
			csvOk = true;
			if ( imageOk )
				view.btnImport.setEnabled( true );
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

		final Map< String, Integer > uncleanHeaderMap = records.getHeaderMap();
		this.headerMap = new HashMap<>( uncleanHeaderMap.size() );
		for ( final String uncleanKey : uncleanHeaderMap.keySet() )
		{
			// Remove control and invisible chars.
			final String cleanKey = uncleanKey.trim().replaceAll( "\\p{C}", "" );
			headerMap.put( cleanKey, uncleanHeaderMap.get( uncleanKey ) );
		}

		// Iterate in column orders.
		final List< String > headers = new ArrayList<>( headerMap.keySet() );
		headers.removeIf( ( e ) -> e.trim().isEmpty() );

		if ( headers.isEmpty() )
		{
			error( "Could not read the header of the CSV file.\nIt does not seem present.\n" );
			return false;
		}

		final String[] mandatory = headers.toArray( new String[] {} );
		view.comboBoxXCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		view.comboBoxYCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
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

			if ( current.toLowerCase().startsWith( "x" ) || current.toLowerCase().endsWith( "x" ) )
			{
				if ( xcol < 0 || ( current.length() < mandatory[ xcol ].length() ) )
					xcol = i;
			}

			if ( current.toLowerCase().startsWith( "y" ) || current.toLowerCase().endsWith( "y" ) )
			{
				if ( ycol < 0 || ( current.length() < mandatory[ ycol ].length() ) )
					ycol = i;
			}

			if ( current.toLowerCase().startsWith( "frame" )
					|| current.toLowerCase().startsWith( "time" )
					|| current.toLowerCase().startsWith( "t" ) )
			{
				if ( tcol < 0 || current.equals( "frame" ) )
					tcol = i;
			}

			if ( current.toLowerCase().startsWith( "track" ) || current.toLowerCase().startsWith( "traj" ) )
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
		view.comboBoxFrameCol.setSelectedIndex( tcol );
		view.comboBoxTrackCol.setSelectedIndex( trackcol );

		// Add a NONE for non mandatory columns
		headers.add( NONE_COLUMN );
		final String[] nonMandatory = headers.toArray( new String[] {} );
		view.comboBoxZCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		view.comboBoxQualityCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		view.comboBoxNameCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		view.comboBoxIDCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );

		int idcol = headers.indexOf( NONE_COLUMN );
		int qualitycol = headers.indexOf( NONE_COLUMN );
		int namecol = headers.indexOf( NONE_COLUMN );
		for ( int i = 0; i < nonMandatory.length; i++ )
		{
			final String current = nonMandatory[ i ];

			if ( current.toLowerCase().startsWith( "z" ) || current.toLowerCase().endsWith( "z" ) )
			{
				if ( zcol < 0 || ( current.length() < mandatory[ zcol ].length() ) )
					zcol = i;
			}

			if ( current.toLowerCase().startsWith( "id" ) )
				idcol = i;

			if ( current.toLowerCase().startsWith( "name" ) )
				namecol = i;

			if ( current.toLowerCase().startsWith( "q" ) )
				qualitycol = i;
		}

		view.comboBoxZCol.setSelectedIndex( zcol );
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
		view.getLogger().error( string );
	}

	private void log( final String string )
	{
		view.getLogger().log( string );
	}
}
