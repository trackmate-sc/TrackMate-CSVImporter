package fiji.plugin.trackmate.importer.roicsv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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
import fiji.plugin.trackmate.exporter.csv.ExporterPanel;
import fiji.plugin.trackmate.exporter.csv.ImagePlusComboBoxModel;
import fiji.plugin.trackmate.exporter.csv.ImagePlusListRenderer;
import ij.ImagePlus;

public class CsvRoiImporterPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	static final ImageIcon ICON = new ImageIcon( ExporterPanel.class.getResource( "TrackMateCSVImporterLogo.png" ) );

	final JTextField textFieldFile;

	final JButton btnBrowse;

	final JButton btnImport;

	final JComboBox< ImagePlus > comboBoxImp;

	final JCheckBox chckbxComputeFeatures;

	private final JTextPane jTextPaneLog;

	private final ExporterLogger logger;

	private final JProgressBar progressBar;

	public CsvRoiImporterPanel()
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
		layout.rowHeights = new int[] { 5, 5, 5, 5, 0 };
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
		gbc_chckbxComputeFeatures.gridx = 0;
		gbc_chckbxComputeFeatures.gridy = 4;
		panelControl.add( chckbxComputeFeatures, gbc_chckbxComputeFeatures );

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

		final JPanel panelButtonExport = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtonExport.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		panelLog.add( panelButtonExport, BorderLayout.SOUTH );

		btnImport = new JButton( "Import" );
		panelButtonExport.add( btnImport );

		this.progressBar = new JProgressBar();
		panelLog.add( progressBar, BorderLayout.NORTH );

		final JPanel panelTitle = new JPanel();
		add( panelTitle, BorderLayout.NORTH );
		panelTitle.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblTrackmateCsvImporter = new JLabel(
				"TrackMate ROI CSV importer v" + VersionUtils.getVersion( CsvRoiImporterPanel.class ),
				ICON,
				JLabel.CENTER );
		lblTrackmateCsvImporter.setFont( bigFont );
		lblTrackmateCsvImporter.setPreferredSize( new Dimension( 100, 50 ) );
		lblTrackmateCsvImporter.setHorizontalAlignment( SwingConstants.CENTER );
		panelTitle.add( lblTrackmateCsvImporter, BorderLayout.NORTH );

		this.logger = new ExporterLogger();

	}

	public Logger getLogger()
	{
		return logger;
	}

	private class ExporterLogger extends Logger
	{
		@Override
		public void log( final String message, final Color color )
		{
			CsvRoiImporterPanel.this.log( message, color );
		}

		@Override
		public void error( final String message )
		{
			CsvRoiImporterPanel.this.log( message, Logger.ERROR_COLOR );
		}

		@Override
		public void setProgress( final double val )
		{
			SwingUtilities.invokeLater( () -> progressBar.setValue( ( int ) ( 100 * val ) ) );
		}

		@Override
		public void setStatus( final String status )
		{
			CsvRoiImporterPanel.this.log( status, Logger.BLUE_COLOR );
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
