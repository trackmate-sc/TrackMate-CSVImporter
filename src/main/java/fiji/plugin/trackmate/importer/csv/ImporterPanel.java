/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2017 - 2022 TrackMate developers.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import ij.ImagePlus;

public class ImporterPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	static final ImageIcon ICON = new ImageIcon( ImporterPanel.class.getResource( "TrackMateCSVImporterLogo.png" ) );

	final JTextField textFieldFile;

	final JButton btnBrowse;

	final JButton btnImport;

	final JComboBox< ImagePlus > comboBoxImp;

	final JComboBox< String > comboBoxXCol;

	final JComboBox< String > comboBoxYCol;

	final JComboBox< String > comboBoxZCol;

	final JComboBox< String > comboBoxFrameCol;

	final JComboBox< String > comboBoxQualityCol;

	final JComboBox< String > comboBoxNameCol;

	final JComboBox< String > comboBoxIDCol;

	final JComboBox< String > comboBoxTrackCol;

	final JCheckBox chckbxImportTracks;

	final JCheckBox chckbxComputeFeatures;

	final JLabel labelRadiusUnit;

	final JFormattedTextField ftfRadius;

	private final JTextPane jTextPaneLog;

	private final ImporterLogger logger;

	private final JProgressBar progressBar;

	public ImporterPanel()
	{
		final Font smallFont = getFont().deriveFont( getFont().getSize2D() - 1f );
		final Font bigFont = getFont().deriveFont( getFont().getSize2D() + 2f ).deriveFont( Font.BOLD );

		setLayout( new BorderLayout() );

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder( null );
		splitPane.setResizeWeight( 0.5 );
		add( splitPane );

		final JPanel panelControl = new JPanel();
		panelControl.setBorder( null );
		splitPane.setLeftComponent( panelControl );
		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 5, 5, 5, 5, 0, 5, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5 };
		layout.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 1.0 };
		layout.columnWidths = new int[] { 79, 50, 30, 50, 30 };
		panelControl.setLayout( layout );

		final JLabel lblCsvFile = new JLabel( "CSV file:" );
		final GridBagConstraints gbc_lblCsvFile = new GridBagConstraints();
		gbc_lblCsvFile.anchor = GridBagConstraints.WEST;
		gbc_lblCsvFile.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblCsvFile.gridx = 0;
		gbc_lblCsvFile.gridy = 0;
		panelControl.add( lblCsvFile, gbc_lblCsvFile );

		textFieldFile = new JTextField();
		final GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets( 5, 5, 5, 0 );
		gbc_textField.gridwidth = 5;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panelControl.add( textFieldFile, gbc_textField );
		textFieldFile.setColumns( 5 );

		btnBrowse = new JButton( "Browse" );
		final GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.gridwidth = 2;
		gbc_btnBrowse.insets = new Insets( 5, 5, 5, 0 );
		gbc_btnBrowse.gridx = 3;
		gbc_btnBrowse.gridy = 2;
		panelControl.add( btnBrowse, gbc_btnBrowse );

		final JLabel lblTargetImage = new JLabel( "Target image:" );
		final GridBagConstraints gbc_lblTargetImage = new GridBagConstraints();
		gbc_lblTargetImage.anchor = GridBagConstraints.EAST;
		gbc_lblTargetImage.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblTargetImage.gridx = 0;
		gbc_lblTargetImage.gridy = 3;
		panelControl.add( lblTargetImage, gbc_lblTargetImage );

		comboBoxImp = new JComboBox<>( new ImagePlusComboBoxModel() );
		comboBoxImp.setRenderer( new ImagePlusListRenderer() );
		final GridBagConstraints gbc_comboBoxImp = new GridBagConstraints();
		gbc_comboBoxImp.gridwidth = 4;
		gbc_comboBoxImp.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxImp.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxImp.gridx = 1;
		gbc_comboBoxImp.gridy = 3;
		panelControl.add( comboBoxImp, gbc_comboBoxImp );

		chckbxComputeFeatures = new JCheckBox( "Compute all features?" );
		chckbxComputeFeatures.setSelected( true );
		final GridBagConstraints gbc_chckbxComputeFeatures = new GridBagConstraints();
		gbc_chckbxComputeFeatures.anchor = GridBagConstraints.EAST;
		gbc_chckbxComputeFeatures.gridwidth = 5;
		gbc_chckbxComputeFeatures.insets = new Insets( 0, 0, 5, 0 );
		gbc_chckbxComputeFeatures.gridx = 0;
		gbc_chckbxComputeFeatures.gridy = 4;
		panelControl.add( chckbxComputeFeatures, gbc_chckbxComputeFeatures );

		chckbxImportTracks = new JCheckBox( "Import tracks?" );
		final GridBagConstraints gbc_chckbxImportTracks = new GridBagConstraints();
		gbc_chckbxImportTracks.anchor = GridBagConstraints.EAST;
		gbc_chckbxImportTracks.gridwidth = 5;
		gbc_chckbxImportTracks.insets = new Insets( 5, 5, 5, 0 );
		gbc_chckbxImportTracks.gridx = 0;
		gbc_chckbxImportTracks.gridy = 5;
		panelControl.add( chckbxImportTracks, gbc_chckbxImportTracks );

		final JSeparator separator = new JSeparator();
		final GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.anchor = GridBagConstraints.WEST;
		gbc_separator.gridwidth = 3;
		gbc_separator.insets = new Insets( 5, 5, 5, 5 );
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 6;
		panelControl.add( separator, gbc_separator );

		final JLabel lblRadius = new JLabel( "Radius:" );
		final GridBagConstraints gbc_lblRadius = new GridBagConstraints();
		gbc_lblRadius.anchor = GridBagConstraints.EAST;
		gbc_lblRadius.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblRadius.gridx = 0;
		gbc_lblRadius.gridy = 7;
		panelControl.add( lblRadius, gbc_lblRadius );

		ftfRadius = new JFormattedTextField( NumberFormat.getNumberInstance() );
		ftfRadius.setHorizontalAlignment( SwingConstants.TRAILING );
		final GridBagConstraints gbc_ftfRadius = new GridBagConstraints();
		gbc_ftfRadius.gridwidth = 2;
		gbc_ftfRadius.insets = new Insets( 5, 5, 5, 5 );
		gbc_ftfRadius.fill = GridBagConstraints.HORIZONTAL;
		gbc_ftfRadius.gridx = 1;
		gbc_ftfRadius.gridy = 7;
		panelControl.add( ftfRadius, gbc_ftfRadius );

		labelRadiusUnit = new JLabel();
		final GridBagConstraints gbc_labelRadiusUnitl = new GridBagConstraints();
		gbc_labelRadiusUnitl.anchor = GridBagConstraints.WEST;
		gbc_labelRadiusUnitl.insets = new Insets( 0, 0, 5, 5 );
		gbc_labelRadiusUnitl.gridx = 3;
		gbc_labelRadiusUnitl.gridy = 7;
		panelControl.add( labelRadiusUnit, gbc_labelRadiusUnitl );

		final JLabel lblXColumn = new JLabel( "X column:" );
		final GridBagConstraints gbc_lblXColumn = new GridBagConstraints();
		gbc_lblXColumn.anchor = GridBagConstraints.EAST;
		gbc_lblXColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblXColumn.gridx = 0;
		gbc_lblXColumn.gridy = 8;
		panelControl.add( lblXColumn, gbc_lblXColumn );

		comboBoxXCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxX = new GridBagConstraints();
		gbc_comboBoxX.gridwidth = 4;
		gbc_comboBoxX.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxX.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxX.gridx = 1;
		gbc_comboBoxX.gridy = 8;
		panelControl.add( comboBoxXCol, gbc_comboBoxX );

		final JLabel lblYColumn = new JLabel( "Y column:" );
		final GridBagConstraints gbc_lblYColumn = new GridBagConstraints();
		gbc_lblYColumn.anchor = GridBagConstraints.EAST;
		gbc_lblYColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblYColumn.gridx = 0;
		gbc_lblYColumn.gridy = 9;
		panelControl.add( lblYColumn, gbc_lblYColumn );

		comboBoxYCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxY = new GridBagConstraints();
		gbc_comboBoxY.gridwidth = 4;
		gbc_comboBoxY.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxY.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxY.gridx = 1;
		gbc_comboBoxY.gridy = 9;
		panelControl.add( comboBoxYCol, gbc_comboBoxY );

		final JLabel lblZColumn = new JLabel( "Z column:" );
		final GridBagConstraints gbc_lblZColumn = new GridBagConstraints();
		gbc_lblZColumn.anchor = GridBagConstraints.EAST;
		gbc_lblZColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblZColumn.gridx = 0;
		gbc_lblZColumn.gridy = 10;
		panelControl.add( lblZColumn, gbc_lblZColumn );

		comboBoxZCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxZ = new GridBagConstraints();
		gbc_comboBoxZ.gridwidth = 4;
		gbc_comboBoxZ.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxZ.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxZ.gridx = 1;
		gbc_comboBoxZ.gridy = 10;
		panelControl.add( comboBoxZCol, gbc_comboBoxZ );

		final JLabel lblFrameColumn = new JLabel( "Frame column:" );
		final GridBagConstraints gbc_lblFrameColumn = new GridBagConstraints();
		gbc_lblFrameColumn.anchor = GridBagConstraints.EAST;
		gbc_lblFrameColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblFrameColumn.gridx = 0;
		gbc_lblFrameColumn.gridy = 11;
		panelControl.add( lblFrameColumn, gbc_lblFrameColumn );

		comboBoxFrameCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxFrame = new GridBagConstraints();
		gbc_comboBoxFrame.gridwidth = 4;
		gbc_comboBoxFrame.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxFrame.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxFrame.gridx = 1;
		gbc_comboBoxFrame.gridy = 11;
		panelControl.add( comboBoxFrameCol, gbc_comboBoxFrame );

		final JLabel lblTrackColumn = new JLabel( "Track column:" );
		final GridBagConstraints gbc_lblTrackColumn = new GridBagConstraints();
		gbc_lblTrackColumn.anchor = GridBagConstraints.EAST;
		gbc_lblTrackColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblTrackColumn.gridx = 0;
		gbc_lblTrackColumn.gridy = 12;
		panelControl.add( lblTrackColumn, gbc_lblTrackColumn );

		comboBoxTrackCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxTrackCol = new GridBagConstraints();
		gbc_comboBoxTrackCol.gridwidth = 4;
		gbc_comboBoxTrackCol.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxTrackCol.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxTrackCol.gridx = 1;
		gbc_comboBoxTrackCol.gridy = 12;
		panelControl.add( comboBoxTrackCol, gbc_comboBoxTrackCol );
		chckbxImportTracks.addActionListener( ( e ) -> comboBoxTrackCol.setEnabled( chckbxImportTracks.isSelected() ) );
		comboBoxTrackCol.setEnabled( chckbxImportTracks.isSelected() );

		final JLabel lblQualityColumn = new JLabel( "Quality column:" );
		final GridBagConstraints gbc_lblQualityColumn = new GridBagConstraints();
		gbc_lblQualityColumn.anchor = GridBagConstraints.EAST;
		gbc_lblQualityColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblQualityColumn.gridx = 0;
		gbc_lblQualityColumn.gridy = 13;
		panelControl.add( lblQualityColumn, gbc_lblQualityColumn );

		comboBoxQualityCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxQuality = new GridBagConstraints();
		gbc_comboBoxQuality.gridwidth = 4;
		gbc_comboBoxQuality.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxQuality.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxQuality.gridx = 1;
		gbc_comboBoxQuality.gridy = 13;
		panelControl.add( comboBoxQualityCol, gbc_comboBoxQuality );

		final JLabel lblNameColumn = new JLabel( "Name column:" );
		final GridBagConstraints gbc_lblNameColumn = new GridBagConstraints();
		gbc_lblNameColumn.anchor = GridBagConstraints.EAST;
		gbc_lblNameColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblNameColumn.gridx = 0;
		gbc_lblNameColumn.gridy = 14;
		panelControl.add( lblNameColumn, gbc_lblNameColumn );

		comboBoxNameCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxName = new GridBagConstraints();
		gbc_comboBoxName.gridwidth = 4;
		gbc_comboBoxName.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxName.gridx = 1;
		gbc_comboBoxName.gridy = 14;
		panelControl.add( comboBoxNameCol, gbc_comboBoxName );

		final JLabel lblIdColumn = new JLabel( "ID column:" );
		final GridBagConstraints gbc_lblIdColumn = new GridBagConstraints();
		gbc_lblIdColumn.anchor = GridBagConstraints.EAST;
		gbc_lblIdColumn.insets = new Insets( 5, 5, 0, 5 );
		gbc_lblIdColumn.gridx = 0;
		gbc_lblIdColumn.gridy = 15;
		panelControl.add( lblIdColumn, gbc_lblIdColumn );

		comboBoxIDCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxID = new GridBagConstraints();
		gbc_comboBoxID.insets = new Insets( 5, 5, 0, 0 );
		gbc_comboBoxID.gridwidth = 4;
		gbc_comboBoxID.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxID.gridx = 1;
		gbc_comboBoxID.gridy = 15;
		panelControl.add( comboBoxIDCol, gbc_comboBoxID );

		final JPanel panelLog = new JPanel();
		splitPane.setRightComponent( panelLog );
		panelLog.setLayout( new BorderLayout() );

		jTextPaneLog = new JTextPane();
		jTextPaneLog.setPreferredSize( new Dimension( 300, 300 ) );
		jTextPaneLog.setBorder( null );
		jTextPaneLog.setEditable( false );
		jTextPaneLog.setForeground( Color.BLACK );
		jTextPaneLog.setFont( smallFont );
		jTextPaneLog.setAutoscrolls( true );

		final JScrollPane scrollPane = new JScrollPane( jTextPaneLog );
		scrollPane.setOpaque( false );
		scrollPane.setBorder( new TitledBorder( new LineBorder( new Color( 128, 128, 128 ) ), "Import log.", TitledBorder.LEADING, TitledBorder.TOP, null, null ) );
		panelLog.add( scrollPane, BorderLayout.CENTER );
		jTextPaneLog.setBackground( getBackground() );

		final JPanel panelButtonImport = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtonImport.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		panelLog.add( panelButtonImport, BorderLayout.SOUTH );

		btnImport = new JButton( "Import" );
		panelButtonImport.add( btnImport );

		this.progressBar = new JProgressBar();
		panelLog.add( progressBar, BorderLayout.NORTH );

		final JPanel panelTitle = new JPanel();
		add( panelTitle, BorderLayout.NORTH );
		panelTitle.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblTrackmateCsvImporter = new JLabel( "TrackMate CSV importer v" + VersionUtils.getVersion( ImporterPanel.class ), ICON, JLabel.CENTER );
		lblTrackmateCsvImporter.setFont( bigFont );
		lblTrackmateCsvImporter.setPreferredSize( new Dimension( 100, 50 ) );
		lblTrackmateCsvImporter.setHorizontalAlignment( SwingConstants.CENTER );
		panelTitle.add( lblTrackmateCsvImporter, BorderLayout.NORTH );

		this.logger = new ImporterLogger();

	}

	public Logger getLogger()
	{
		return logger;
	}

	private class ImporterLogger extends Logger
	{
		@Override
		public void log( final String message, final Color color )
		{
			ImporterPanel.this.log( message, color );
		}

		@Override
		public void error( final String message )
		{
			ImporterPanel.this.log( message, Logger.ERROR_COLOR );
		}

		@Override
		public void setProgress( final double val )
		{
			SwingUtilities.invokeLater( () -> progressBar.setValue( ( int ) ( 100 * val ) ) );
		}

		@Override
		public void setStatus( final String status )
		{
			ImporterPanel.this.log( status, Logger.BLUE_COLOR );
		}
	}

	private void log( final String message, final Color color )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final StyleContext sc = StyleContext.getDefaultStyleContext();
				final AttributeSet aset = sc.addAttribute( SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color );
				final Document doc = jTextPaneLog.getDocument();
				try
				{
					doc.insertString( doc.getLength(), message, aset );
				}
				catch ( final BadLocationException e )
				{
					e.printStackTrace();
				}
			}
		} );
	}
}
