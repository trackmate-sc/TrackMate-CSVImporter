package fiji.plugin.trackmate.exporter.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Optional;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.model.primitives.PositiveInteger;

public class ImporterUtils
{

	public static final long countLineNumber( final String file )
	{
		long nLines = 0;
		try (final LineNumberReader lineNumberReader = new LineNumberReader( new FileReader( file ) ))
		{
			lineNumberReader.skip( Long.MAX_VALUE );
			nLines = lineNumberReader.getLineNumber();
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "FileNotFoundException Occurred" + e.getMessage() );
		}
		catch ( final IOException e )
		{
			System.out.println( "IOException Occurred" + e.getMessage() );
		}
		return nLines;
	}

	public static Settings createSettingsFromImageFile( final String imageFile, final StringBuilder errorHolder, final Logger logger )
	{
		return createSettingsFromImageFile( imageFile, 0, errorHolder, logger );
	}

	private static Settings createSettingsFromImageFile( final String imageFile, final int series, final StringBuilder errorHolder, final Logger logger )
	{
		logger.log( "Creating settings from image file.\n" );
		final Settings settings = new Settings();
		try
		{
			DebugTools.setRootLevel( "ERROR" );
			final ImporterOptions options = new ImporterOptions();
			options.setId( imageFile );
			options.setQuiet( true );
			options.setWindowless( true );
			final ImportProcess process = new ImportProcess( options );
			if ( !process.execute() )
			{
				errorHolder.append( "Error while preparing the import of metadata." );
				return null;
			}

			final IMetadata metadata = process.getOMEMetadata();
			final Length pixelsPhysicalSizeX = metadata.getPixelsPhysicalSizeX( series );
			final Length pixelsPhysicalSizeY = metadata.getPixelsPhysicalSizeY( series );
			final Length pixelsPhysicalSizeZ = metadata.getPixelsPhysicalSizeZ( series );
			final Time timeIncrement = metadata.getPixelsTimeIncrement( series );
			final PositiveInteger sizeX = metadata.getPixelsSizeX( series );
			final PositiveInteger sizeY = metadata.getPixelsSizeY( series );
			final PositiveInteger sizeZ = metadata.getPixelsSizeZ( series );
			final PositiveInteger sizeT = metadata.getPixelsSizeT( series );

			settings.width = sizeX.getValue().intValue();
			settings.height = sizeY.getValue().intValue();
			settings.nslices = sizeZ.getValue().intValue();
			settings.nframes = sizeT.getValue().intValue();
			settings.dx = pixelsPhysicalSizeX.value().doubleValue();
			settings.dy = pixelsPhysicalSizeY.value().doubleValue();
			settings.dz = Optional.ofNullable( pixelsPhysicalSizeZ )
					.orElse( new Length( Double.valueOf( 1. ), UNITS.PIXEL ) )
					.value().doubleValue();
			settings.dt = timeIncrement.value().doubleValue();
			settings.xstart = 0;
			settings.xend = settings.width - 1;
			settings.ystart = 0;
			settings.yend = settings.height - 1;
			settings.zstart = 0;
			settings.zend = settings.nslices - 1;
			final File file = new File( imageFile );
			settings.imageFileName = file.getName();
			settings.imageFolder = file.getParent();
		}
		catch ( final IOException | FormatException e )
		{
			errorHolder.append( "Problem reading metadata:\n" + e.getMessage() );
			return null;
		}
		return settings;
	}

	public static String[] getUnitsFromImageFile( final String imageFilePath, final StringBuilder errorHolder )
	{
		return getUnitsFromImageFile( imageFilePath, 0, errorHolder );
	}

	private static String[] getUnitsFromImageFile( final String imageFilePath, final int series, final StringBuilder errorHolder )
	{
		try
		{
			DebugTools.setRootLevel( "ERROR" );
			final ImporterOptions options = new ImporterOptions();
			options.setId( imageFilePath );
			options.setQuiet( true );
			options.setWindowless( true );
			final ImportProcess process = new ImportProcess( options );
			if ( !process.execute() )
			{
				errorHolder.append( "Error while preparing the import of metadata." );
				return null;
			}

			final IMetadata metadata = process.getOMEMetadata();
			final String spaceUnit = metadata.getPixelsPhysicalSizeX( series ).unit().getSymbol();
			final String timeUnit = metadata.getPixelsTimeIncrement( series ).unit().getSymbol();
			return new String[] { spaceUnit, timeUnit };
		}
		catch ( final IOException | FormatException e )
		{
			errorHolder.append( "Problem reading metadata:\n" + e.getMessage() );
			return null;
		}
	}

	public static Settings createSettingsFromImp( final ImagePlus imp, final Logger logger )
	{
		logger.log( "Creating settings from opened ImagePlus.\n" );
		final Settings settings = new Settings();
		settings.setFrom( imp );
		return settings;
	}
}
