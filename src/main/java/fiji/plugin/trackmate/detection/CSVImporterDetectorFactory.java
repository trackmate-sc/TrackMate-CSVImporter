package fiji.plugin.trackmate.detection;

import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeRadius;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMatePlugIn;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotDetectorFactory.class, enabled = false )
public class CSVImporterDetectorFactory< T extends RealType< T > & NativeType< T > > implements SpotDetectorFactory< T >
{

	public static final String INFO_TEXT = "<html>"
			+ "This detector does not operate on the provided image "
			+ "but instead loads a CSV file and parse it to provide "
			+ "spots to TrackMate."
			+ "<p>"
			+ "The CSV file must end in .csv, have a header line at the "
			+ "first line of the file, and have detections organized by rows, "
			+ "with at least columns for X, Y, Z and frame."
			+ "</html>";

	public static final String KEY = "CSV_IMPORTER_DETECTOR";

	public static final String NAME = "CSV importer";

	/**
	 * The key for the file path setting. Expected values are {@link String}s,
	 * which represents a valid file path to a CSV file.
	 * <p>
	 * This setting is mandatory.
	 */
	public static final String KEY_FILE_PATH = "FILE_PATH";

	/**
	 * The key for the X column name in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the X
	 * position of the particles, in physical coordinates.
	 * <p>
	 * This setting is mandatory.
	 */
	public static final String KEY_X_COLUMN_NAME = "X_COLUMN";

	/**
	 * The key for the Y column name in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the Y
	 * position of the particles, in physical coordinates.
	 * <p>
	 * This setting is mandatory.
	 */
	public static final String KEY_Y_COLUMN_NAME = "Y_COLUMN";

	/**
	 * The key for the Z column name in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the Z
	 * position of the particles, in physical coordinates.
	 * <p>
	 * This setting is mandatory.
	 */
	public static final String KEY_Z_COLUMN_NAME = "Z_COLUMN";

	/**
	 * The key for the radius column name in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the radius
	 * of the particles, in physical units. If the value of this parameter is
	 * <code>null</code>, particles will be created with a fixed radius
	 * specified by the {@link #KEY_RADIUS} settings.
	 */
	public static final String KEY_RADIUS_COLUMN_NAME = "RADIUS_COLUMN";

	/**
	 * The key for the radius settings. Expected values are strictly positive
	 * {@link Double}s, that specify the radius of the created spot in physical
	 * units.
	 * <p>
	 * The value of this parameter must not be <code>null</code>. The value of
	 * this parameter is used only if the parameter for
	 * {@link #KEY_RADIUS_COLUMN_NAME} is <code>null</code>.
	 */
	public static final String KEY_RADIUS = "RADIUS";

	/**
	 * The key for the frame column name in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the frame
	 * number in which particles are, listed as integers.
	 * <p>
	 * This setting is mandatory.
	 */
	public static final String KEY_FRAME_COLUMN_NAME = "FRAME_COLUMN";

	/**
	 * The key for the track column in the CSV file. Expected values are
	 * {@link String}s which should be the name of a column holding the track
	 * index of particles, listed as integers. This setting is not mandatory.
	 * Tracks will not be imported if the value is empty or <code>null</code>.
	 */
	public static final String KEY_TRACK_COLUMN_NAME = "TRACK_COLUMN";

	/**
	 * The key for the quality value column name in the CSV file. Expected
	 * values are {@link String}s which should be the name of a column holding
	 * the particle quality as a real number.
	 * <p>
	 * This setting is not mandatory. A default value will be used if the value
	 * is empty or <code>null</code>.
	 */
	public static final String KEY_QUALITY_COLUMN_NAME = "QUALITY_COLUMN";

	/**
	 * The key for the particle name column name in the CSV file. Expected
	 * values are {@link String}s which should be the name of a column holding
	 * the particle name as a string.
	 * <p>
	 * This setting is not mandatory. The spot names will be set to
	 * <code>null</code> if the value is empty or <code>null</code>.
	 */
	public static final String KEY_NAME_COLUMN_NAME = "NAME_COLUMN";

	/**
	 * The key for the particle ID column name in the CSV file. Expected values
	 * are {@link String}s which should be the name of a column holding the
	 * particle ID as a string.
	 * <p>
	 * This setting is not mandatory. Only use this setting for debugging
	 * reasons. The new spots will have an ID equal to the one specified in the
	 * SV file column. If the value is empty or <code>null</code>, default IDs
	 * will be used.
	 */
	public static final String KEY_ID_COLUMN_NAME = "ID_COLUMN";

	/**
	 * The key for the X origin settings.
	 * <p>
	 * This origin is relative to the top-left-bottom corner of the image. It
	 * must be given in physical units. Expected values are {@link Double}s.
	 */
	public static final String KEY_X_ORIGIN = "X_ORIGIN";

	/**
	 * The key for the Y origin settings.
	 * <p>
	 * This origin is relative to the top-left-bottom corner of the image. It
	 * must be given in physical units. Expected values are {@link Double}s.
	 */
	public static final String KEY_Y_ORIGIN = "Y_ORIGIN";

	/**
	 * The key for the Z origin settings.
	 * <p>
	 * This origin is relative to the top-left-bottom corner of the image. It
	 * must be given in physical units. Expected values are {@link Double}s.
	 */
	public static final String KEY_Z_ORIGIN = "Z_ORIGIN";

	/**
	 * The default file path.
	 */
	public static final String DEFAULT_FILE_PATH = null;

	/**
	 * The default X column name.
	 */
	public static final String DEFAULT_X_COLUMN_NAME = "x[nm]";

	/**
	 * The default Y column name.
	 */
	public static final String DEFAULT_Y_COLUMN_NAME = "y[nm]";

	/**
	 * The default Z column name.
	 */
	public static final String DEFAULT_Z_COLUMN_NAME = "z[nm]";

	/**
	 * The default frame column name.
	 */
	public static final String DEFAULT_FRAME_COLUMN_NAME = "frame";

	public static final String DEFAULT_QUALITY_COLUMN_NAME = "";

	public static final String DEFAULT_NAME_COLUMN_NAME = "";

	public static final String DEFAULT_ID_COLUMN_NAME = "";

	public static final Double DEFAULT_X_ORIGIN = Double.valueOf( 0. );

	public static final Double DEFAULT_Y_ORIGIN = Double.valueOf( 0. );

	public static final Double DEFAULT_Z_ORIGIN = Double.valueOf( 0. );

	public static final Double DEFAULT_RADIUS = Double.valueOf( 1. );

	private String errorMessage;

	private Map< Integer, List< Spot > > spots;

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		return new DummySpotDetector( frame );
	}

	@Override
	public boolean setTarget( final ImgPlus< T > img, final Map< String, Object > settings )
	{
		final boolean ok = checkSettings( settings );
		if ( !ok )
			return false;

		final String filePath = ( String ) settings.get( KEY_FILE_PATH );
		final double radius = ( Double ) settings.get( KEY_RADIUS );
		final String xColumnName = ( String ) settings.get( KEY_X_COLUMN_NAME );
		final String yColumnName = ( String ) settings.get( KEY_Y_COLUMN_NAME );
		final String zColumnName = ( String ) settings.get( KEY_Z_COLUMN_NAME );
		final String frameColumnName = ( String ) settings.get( KEY_FRAME_COLUMN_NAME );
		final String qualityColumn = ( String ) settings.get( KEY_QUALITY_COLUMN_NAME );
		final String nameColumn = ( String ) settings.get( KEY_NAME_COLUMN_NAME );
		final String idColumn = ( String ) settings.get( KEY_ID_COLUMN_NAME );
		final double xOrigin = ( ( Number ) settings.get( KEY_X_ORIGIN ) ).doubleValue();
		final double yOrigin = ( ( Number ) settings.get( KEY_Y_ORIGIN ) ).doubleValue();
		final double zOrigin = ( ( Number ) settings.get( KEY_Z_ORIGIN ) ).doubleValue();

		final CSVImporter importer = new CSVImporter( filePath, radius,
				xColumnName, yColumnName, zColumnName, frameColumnName,
				qualityColumn, nameColumn, idColumn,
				xOrigin, yOrigin, zOrigin );

		if ( !importer.checkInput() || !importer.process() )
		{
			this.errorMessage = importer.getErrorMessage();
			return false;
		}

		this.spots = importer.getResult();

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		final StringBuilder errorHolder = new StringBuilder();
		final boolean ok =
				writeRadius( settings, element, errorHolder )
						&& writeAttribute( settings, element, KEY_FILE_PATH, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_X_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_Y_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_Z_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_FRAME_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_QUALITY_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_NAME_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_ID_COLUMN_NAME, String.class, errorHolder )
						&& writeAttribute( settings, element, KEY_X_ORIGIN, Double.class, errorHolder )
						&& writeAttribute( settings, element, KEY_Y_ORIGIN, Double.class, errorHolder )
						&& writeAttribute( settings, element, KEY_Z_ORIGIN, Double.class, errorHolder );

		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		settings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true;
		ok = ok & readDoubleAttribute( element, settings, KEY_RADIUS, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_FILE_PATH, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_X_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_Y_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_Z_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_FRAME_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_QUALITY_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_NAME_COLUMN_NAME, errorHolder );
		ok = ok & readStringAttribute( element, settings, KEY_ID_COLUMN_NAME, errorHolder );
		ok = ok & readDoubleAttribute( element, settings, KEY_X_ORIGIN, errorHolder );
		ok = ok & readDoubleAttribute( element, settings, KEY_Y_ORIGIN, errorHolder );
		ok = ok & readDoubleAttribute( element, settings, KEY_Z_ORIGIN, errorHolder );

		if ( !ok )
		{
			errorMessage = errorHolder.toString();
			return false;
		}
		return checkSettings( settings );
	}

	@Override
	public ConfigurationPanel getDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		return new CSVImporterConfigPanel( settings, model );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > map = new HashMap<>();
		map.put( KEY_FILE_PATH, DEFAULT_FILE_PATH );
		map.put( KEY_X_COLUMN_NAME, DEFAULT_X_COLUMN_NAME );
		map.put( KEY_Y_COLUMN_NAME, DEFAULT_Y_COLUMN_NAME );
		map.put( KEY_Z_COLUMN_NAME, DEFAULT_Z_COLUMN_NAME );
		map.put( KEY_FRAME_COLUMN_NAME, DEFAULT_FRAME_COLUMN_NAME );
		map.put( KEY_QUALITY_COLUMN_NAME, DEFAULT_QUALITY_COLUMN_NAME );
		map.put( KEY_NAME_COLUMN_NAME, DEFAULT_NAME_COLUMN_NAME );
		map.put( KEY_ID_COLUMN_NAME, DEFAULT_ID_COLUMN_NAME );
		map.put( KEY_RADIUS, DEFAULT_RADIUS );
		map.put( KEY_X_ORIGIN, DEFAULT_X_ORIGIN );
		map.put( KEY_Y_ORIGIN, DEFAULT_Y_ORIGIN );
		map.put( KEY_Z_ORIGIN, DEFAULT_Z_ORIGIN );
		map.put( KEY_RADIUS_COLUMN_NAME, null );
		map.put( KEY_RADIUS, DEFAULT_RADIUS );
		return map;
	}

	@Override
	public boolean checkSettings( final Map< String, Object > settings )
	{
		boolean ok = true;
		final StringBuilder errorHolder = new StringBuilder();
		ok = ok & checkParameter( settings, KEY_FILE_PATH, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_RADIUS, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_X_COLUMN_NAME, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_Y_COLUMN_NAME, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_Z_COLUMN_NAME, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_FRAME_COLUMN_NAME, String.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_RADIUS, Double.class, errorHolder );
		final List< String > mandatoryKeys = new ArrayList< String >();
		mandatoryKeys.add( KEY_FILE_PATH );
		mandatoryKeys.add( KEY_RADIUS );
		mandatoryKeys.add( KEY_X_COLUMN_NAME );
		mandatoryKeys.add( KEY_Y_COLUMN_NAME );
		mandatoryKeys.add( KEY_Z_COLUMN_NAME );
		mandatoryKeys.add( KEY_FRAME_COLUMN_NAME );
		mandatoryKeys.add( KEY_X_ORIGIN );
		mandatoryKeys.add( KEY_Y_ORIGIN );
		mandatoryKeys.add( KEY_Z_ORIGIN );
		final List< String > optionalKeys = new ArrayList< String >();
		optionalKeys.add( KEY_ID_COLUMN_NAME );
		optionalKeys.add( KEY_NAME_COLUMN_NAME );
		optionalKeys.add( KEY_QUALITY_COLUMN_NAME );
		ok = ok & checkMapKeys( settings, mandatoryKeys, optionalKeys, errorHolder );
		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	/*
	 * PRIVATE CLASSES
	 */

	private class DummySpotDetector implements SpotDetector< T >
	{

		private final int frame;

		public DummySpotDetector( final int frame )
		{
			this.frame = frame;
		}

		@Override
		public List< Spot > getResult()
		{
			List< Spot > s = spots.get( Integer.valueOf( frame ) );
			if ( null == s )
				s = Collections.emptyList();
			return s;
		}

		@Override
		public boolean checkInput()
		{
			return true;
		}

		@Override
		public boolean process()
		{
			return true;
		}

		@Override
		public String getErrorMessage()
		{
			return "";
		}

		@Override
		public long getProcessingTime()
		{
			return 0;
		}

	}

	/*
	 * STATIC UTILITIES
	 */

	private static final boolean readStringAttribute( final Element element, final Map< String, Object > settings, final String parameterKey, final StringBuilder errorHolder )
	{
		final String str = element.getAttributeValue( parameterKey );
		if ( null == str )
		{
			errorHolder.append( "Attribute " + parameterKey + " could not be found in XML element.\n" );
			return false;
		}
		settings.put( parameterKey, str );
		return true;
	}

	public static void main( final String[] args ) throws IOException
	{
		ImageJ.main( args );
		final ImagePlus imp = IJ.openImage( "samples/test_DH1_670_100x_1_49_Microtubules_001-1.30x30.tif" );
		imp.show();
//		final Roi roi = new RoiDecoder( "samples/SN4_GEMS1_016_ROI_001.roi" ).getRoi();
//		imp.setRoi( roi );
		new TrackMatePlugIn().run( "" );
	}
}
