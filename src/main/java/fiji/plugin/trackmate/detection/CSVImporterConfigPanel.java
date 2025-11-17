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
package fiji.plugin.trackmate.detection;

import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FILE_PATH;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_FRAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_ID_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_NAME_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_QUALITY_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_X_ORIGIN;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Y_ORIGIN;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_COLUMN_NAME;
import static fiji.plugin.trackmate.detection.CSVImporterDetectorFactory.KEY_Z_ORIGIN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_RADIUS;
import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;

public class CSVImporterConfigPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final String NONE_COLUMN = "Don't use";

	private final JTextField textFieldFilePath;

	private final JFormattedTextField textFieldRadius;

	private final JComboBox< String > comboBoxXCol;

	private final JComboBox< String > comboBoxYCol;

	private final JComboBox< String > comboBoxZCol;

	private final JComboBox< String > comboBoxFrameCol;

	private final JComboBox< String > comboBoxQualityCol;

	private final JComboBox< String > comboBoxNameCol;

	private final JComboBox< String > comboBoxIDCol;

	private final JLabel lblMessage;

	private final ArrayList< JComboBox< String > > comboBoxes;

	private File file;

	private final JFormattedTextField textFieldXOrigin;

	private final JFormattedTextField textFieldYOrigin;

	private final JFormattedTextField textFieldZOrigin;

	public CSVImporterConfigPanel( final Settings setting, final Model model )
	{
		// Compute origin in physical coordinates.
		final double dx = setting.dx * setting.getXstart();
		final double dy = setting.dy * setting.getYstart();
		final double dz = setting.dz * setting.zstart;

		// Layout GUI.

		final JLabel lblLbl1 = new JLabel( "Settings for detector:" );
		lblLbl1.setFont( FONT );

		final JLabel lblCsvImporter = new JLabel( "CSV Importer" );
		lblCsvImporter.setHorizontalAlignment( SwingConstants.CENTER );
		lblCsvImporter.setFont( BIG_FONT );

		final JLabel lblVsvFile = new JLabel( "CSV file:" );
		lblVsvFile.setFont( FONT );

		textFieldFilePath = new JTextField();
		textFieldFilePath.setColumns( 10 );
		textFieldFilePath.setFont( FONT );

		final JButton btnBrowse = new JButton( "Browse" );
		btnBrowse.setFont( FONT );
		btnBrowse.addActionListener( ( e ) -> browse() );

		this.lblMessage = new JLabel( "" );
		lblMessage.setFont( SMALL_FONT );
		lblMessage.setText( CSVImporterDetectorFactory.INFO_TEXT );

		final JLabel lblXColumn = new JLabel( "X column:" );
		lblXColumn.setFont( FONT );

		this.comboBoxXCol = new JComboBox<>();
		comboBoxXCol.setFont( FONT );

		final JLabel lblYColumn = new JLabel( "Y column:" );
		lblYColumn.setFont( FONT );

		this.comboBoxYCol = new JComboBox<>();
		comboBoxYCol.setFont( FONT );

		final JLabel lblZColumn = new JLabel( "Z column:" );
		lblZColumn.setFont( FONT );

		this.comboBoxZCol = new JComboBox<>();
		comboBoxZCol.setFont( FONT );

		final JLabel lblQualityCol = new JLabel( "Quality column:" );
		lblQualityCol.setFont( FONT );

		this.comboBoxQualityCol = new JComboBox<>();
		comboBoxQualityCol.setFont( FONT );

		final JLabel lblNameColumn = new JLabel( "Name column:" );
		lblNameColumn.setFont( FONT );

		this.comboBoxNameCol = new JComboBox<>();
		comboBoxNameCol.setFont( FONT );

		final JLabel lblIdColumn = new JLabel( "ID column:" );
		lblIdColumn.setFont( FONT );

		this.comboBoxIDCol = new JComboBox<>();
		comboBoxIDCol.setFont( FONT );

		this.comboBoxFrameCol = new JComboBox<>();
		comboBoxFrameCol.setFont( FONT );

		final JLabel lblFrameColumn = new JLabel( "Frame column:" );
		lblFrameColumn.setFont( FONT );

		comboBoxes = new ArrayList<>();
		comboBoxes.add( comboBoxXCol );
		comboBoxes.add( comboBoxYCol );
		comboBoxes.add( comboBoxZCol );
		comboBoxes.add( comboBoxFrameCol );
		comboBoxes.add( comboBoxQualityCol );
		comboBoxes.add( comboBoxNameCol );
		comboBoxes.add( comboBoxIDCol );

		final JLabel lblDisplayRadius = new JLabel( "Display radius:" );
		lblDisplayRadius.setFont( FONT );

		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		final DecimalFormat format = ( DecimalFormat ) nf;
		format.setMaximumFractionDigits( 3 );
		format.setGroupingUsed( false );
		format.setDecimalSeparatorAlwaysShown( true );

		textFieldRadius = new JFormattedTextField( format );
		textFieldRadius.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldRadius.setColumns( 10 );
		textFieldRadius.setFont( FONT );
		textFieldRadius.setValue( Double.valueOf( 0.5 * setting.dx ) );

		final JLabel lblUnits = new JLabel( model.getSpaceUnits() );
		lblUnits.setFont( FONT );

		final JPanel view = new JPanel();

		final JLabel lblXOrigin = new JLabel( "X origin:" );
		lblXOrigin.setFont( FONT );

		textFieldXOrigin = new JFormattedTextField( format );
		textFieldXOrigin.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldXOrigin.setColumns( 10 );
		textFieldXOrigin.setFont( FONT );
		textFieldXOrigin.setValue( Double.valueOf( dx ) );

		final JLabel lblXOriginUnits = new JLabel( model.getSpaceUnits() );
		lblXOriginUnits.setFont( FONT );

		final JLabel lblYOrigin = new JLabel( "Y origin:" );
		lblYOrigin.setFont( FONT );

		textFieldYOrigin = new JFormattedTextField( format );
		textFieldYOrigin.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldYOrigin.setColumns( 10 );
		textFieldYOrigin.setFont( FONT );
		textFieldYOrigin.setValue( Double.valueOf( dy ) );

		final JLabel lblYOriginUnits = new JLabel( model.getSpaceUnits() );
		lblYOriginUnits.setFont( FONT );

		final JLabel lblZOrigin = new JLabel( "Z origin:" );
		lblZOrigin.setFont( FONT );

		textFieldZOrigin = new JFormattedTextField( format );
		textFieldZOrigin.setHorizontalAlignment( SwingConstants.CENTER );
		textFieldZOrigin.setColumns( 10 );
		textFieldZOrigin.setFont( FONT );
		textFieldZOrigin.setValue( Double.valueOf( dz ) );

		final JLabel lblZOriginUnits = new JLabel( model.getSpaceUnits() );
		lblZOriginUnits.setFont( FONT );

		final GroupLayout groupLayout = new GroupLayout( view );
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup( Alignment.LEADING ).addGroup( groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
								.addComponent( lblMessage, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE )
								.addGroup( groupLayout.createSequentialGroup()
										.addGroup( groupLayout.createParallelGroup( Alignment.TRAILING )
												.addComponent( lblCsvImporter, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE )
												.addComponent( lblLbl1, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE )
												.addComponent( textFieldFilePath, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE )
												.addComponent( btnBrowse )
												.addGroup( groupLayout.createSequentialGroup()
														.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
																.addComponent( lblFrameColumn, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblYColumn, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblXColumn, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblZColumn, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblQualityCol, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblNameColumn, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblIdColumn, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblXOrigin, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblYOrigin, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE )
																.addComponent( lblZOrigin, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE ) )
														.addPreferredGap( ComponentPlacement.RELATED )
														.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
																.addComponent( comboBoxFrameCol, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxIDCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxNameCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxQualityCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxYCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxXCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addComponent( comboBoxZCol, Alignment.TRAILING, 0, 158, Short.MAX_VALUE )
																.addGroup( groupLayout.createSequentialGroup()
																		.addGroup( groupLayout.createParallelGroup( Alignment.TRAILING )
																				.addComponent( textFieldYOrigin, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE )
																				.addComponent( textFieldXOrigin, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE )
																				.addComponent( textFieldZOrigin, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE ) )
																		.addPreferredGap( ComponentPlacement.RELATED )
																		.addGroup( groupLayout.createParallelGroup( Alignment.LEADING )
																				.addComponent( lblXOriginUnits, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE )
																				.addComponent( lblYOriginUnits, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE )
																				.addComponent( lblZOriginUnits, GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE ) )
																		.addPreferredGap( ComponentPlacement.RELATED ) ) ) )
												.addGroup( groupLayout.createSequentialGroup()
														.addGap( 6 )
														.addComponent( lblVsvFile, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE ) )
												.addGroup( groupLayout.createSequentialGroup()
														.addComponent( lblDisplayRadius, GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE )
														.addPreferredGap( ComponentPlacement.RELATED )
														.addComponent( textFieldRadius, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE )
														.addPreferredGap( ComponentPlacement.RELATED )
														.addComponent( lblUnits, GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE )
														.addPreferredGap( ComponentPlacement.RELATED ) ) )
										.addGap( 0 ) ) ) ) );
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup( Alignment.LEADING )
						.addGroup( groupLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent( lblLbl1 )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( lblCsvImporter )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( lblMessage, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblDisplayRadius )
										.addComponent( textFieldRadius, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblUnits ) )
								.addPreferredGap( ComponentPlacement.UNRELATED )
								.addComponent( lblVsvFile )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( textFieldFilePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addComponent( btnBrowse )
								.addPreferredGap( ComponentPlacement.UNRELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblXColumn )
										.addComponent( comboBoxXCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxYCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblYColumn ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxZCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblZColumn ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxFrameCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblFrameColumn ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxQualityCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblQualityCol ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxNameCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblNameColumn ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( comboBoxIDCol, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblIdColumn ) )
								.addPreferredGap( ComponentPlacement.RELATED ).addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblXOrigin )
										.addComponent( textFieldXOrigin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblXOriginUnits ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblYOrigin )
										.addComponent( textFieldYOrigin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblYOriginUnits ) )
								.addPreferredGap( ComponentPlacement.RELATED )
								.addGroup( groupLayout.createParallelGroup( Alignment.BASELINE )
										.addComponent( lblZOrigin )
										.addComponent( textFieldZOrigin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
										.addComponent( lblZOriginUnits ) )
								.addContainerGap( 43, Short.MAX_VALUE ) ) );

		view.setLayout( groupLayout );
		view.setPreferredSize( new Dimension( 200, 600 ) );

		final JScrollPane scrollPane = new JScrollPane( view );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( BorderFactory.createEmptyBorder() );

		setLayout( new BorderLayout() );
		this.add( scrollPane, BorderLayout.CENTER );
	}

	private void browse()
	{
		final File file = askForCSVfile();
		if ( null == file )
			return;

		textFieldFilePath.setText( file.getAbsolutePath() );
		readHeaders();
	}

	private File askForCSVfile()
	{
		if ( null == file )
			this.file = new File( System.getProperty( "user.home" ) );

		final FileDialog dialog = new FileDialog( new JFrame(), "Open a CSV file", FileDialog.LOAD );
		dialog.setIconImage( TRACKMATE_ICON.getImage() );
		dialog.setDirectory( file.getParent() );
		dialog.setFile( file.getName() );
		final FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept( final File dir, final String name )
			{
				return name.endsWith( ".csv" );
			}
		};
		dialog.setFilenameFilter( filter );
		dialog.setVisible( true );
		String selectedFile = dialog.getFile();
		if ( null == selectedFile ) { return null; }
		if ( !selectedFile.endsWith( ".csv" ) )
			selectedFile += ".csv";
		file = new File( dialog.getDirectory(), selectedFile );
		return file;
	}

	@Override
	public void clean()
	{}

	@Override
	public Map< String, Object > getSettings()
	{
		final Map< String, Object > map = new HashMap<>();

		textFieldRadius.validate();
		final Number val = ( Number ) textFieldRadius.getValue();
		map.put( KEY_RADIUS, val.doubleValue() );

		map.put( KEY_FILE_PATH, textFieldFilePath.getText() );
		map.put( KEY_FRAME_COLUMN_NAME, comboBoxFrameCol.getSelectedItem() );
		map.put( KEY_X_COLUMN_NAME, comboBoxXCol.getSelectedItem() );
		map.put( KEY_Y_COLUMN_NAME, comboBoxYCol.getSelectedItem() );
		map.put( KEY_Z_COLUMN_NAME, comboBoxZCol.getSelectedItem() );

		Object qualityCol = comboBoxQualityCol.getSelectedItem();
		if ( qualityCol == NONE_COLUMN )
			qualityCol = "";
		map.put( KEY_QUALITY_COLUMN_NAME, qualityCol );

		Object nameCol = comboBoxNameCol.getSelectedItem();
		if ( nameCol == NONE_COLUMN )
			nameCol = "";
		map.put( KEY_NAME_COLUMN_NAME, nameCol );

		Object idCol = comboBoxIDCol.getSelectedItem();
		if ( idCol == NONE_COLUMN )
			idCol = "";
		map.put( KEY_ID_COLUMN_NAME, idCol );

		textFieldXOrigin.validate();
		map.put( KEY_X_ORIGIN, textFieldXOrigin.getValue() );
		textFieldYOrigin.validate();
		map.put( KEY_Y_ORIGIN, textFieldYOrigin.getValue() );
		textFieldZOrigin.validate();
		map.put( KEY_Z_ORIGIN, textFieldZOrigin.getValue() );

		return map;
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		final String filePath = ( String ) settings.get( KEY_FILE_PATH );
		textFieldFilePath.setText( filePath );

		if ( null != textFieldFilePath.getText() && !textFieldFilePath.getText().isEmpty() )
			readHeaders();
	}

	private void readHeaders()
	{
		final String filePath = textFieldFilePath.getText();

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
			e.printStackTrace();
			e.getMessage();
			return;
		}

		try
		{
			final CSVFormat csvFormat = CSVFormat.EXCEL
					.builder()
					.setHeader()
					.setCommentMarker( '#' )
					.get();
			records = csvFormat.parse( in );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			lblMessage.setText( "<html>" + e.getMessage() + "</html>" );
			clearComboBoxes();
			return;
		}

		final Map< String, Integer > headerMap = records.getHeaderMap();

		// Iterate in column orders.
		final ArrayList< String > headers = new ArrayList<>( headerMap.keySet() );

		if ( headers.isEmpty() )
		{
			lblMessage.setText( "Could not read the header of the CSV file." );
			return;
		}

		final String[] mandatory = headers.toArray( new String[] {} );
		comboBoxXCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		comboBoxYCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		comboBoxZCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
		comboBoxFrameCol.setModel( new DefaultComboBoxModel<>( mandatory ) );

		// Try to be clever and guess from header names.
		int tcol = -1;
		int xcol = -1;
		int ycol = -1;
		int zcol = -1;
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
				if (tcol < 0 || current.equals( "frame" ) )
					tcol = i;
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

		comboBoxXCol.setSelectedIndex( xcol );
		comboBoxYCol.setSelectedIndex( ycol );
		comboBoxZCol.setSelectedIndex( zcol );
		comboBoxFrameCol.setSelectedIndex( tcol );

		// Add a NONE for non mandatory columns
		headers.add( NONE_COLUMN );
		final String[] nonMandatory = headers.toArray( new String[] {} );
		comboBoxQualityCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		comboBoxNameCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
		comboBoxIDCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );

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

		comboBoxIDCol.setSelectedIndex( idcol );
		comboBoxQualityCol.setSelectedIndex( qualitycol );
		comboBoxNameCol.setSelectedIndex( namecol );
	}

	private void clearComboBoxes()
	{
		for ( final JComboBox< String > cb : comboBoxes )
			cb.setModel( new DefaultComboBoxModel<>() );
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );
		final JFrame frame = new JFrame( "CSV Importer" );

		final Settings settings = new Settings();
		settings.dx = 0.2;
		settings.dy = 0.2;
		settings.dz = 1.;
		settings.zstart = 23;

		final Model model = new Model();
		model.setPhysicalUnits( "µm", "s" );

		frame.getContentPane().add( new CSVImporterConfigPanel( settings, model ) );
		frame.pack();
		frame.setVisible( true );
	}
}
