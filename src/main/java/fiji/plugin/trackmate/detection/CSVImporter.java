/*-
 * #%L
 * A CSV importer for TrackMate.
 * %%
 * Copyright (C) 2017 - 2022 Institut Pasteur.
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import fiji.plugin.trackmate.Spot;
import net.imglib2.algorithm.OutputAlgorithm;

public class CSVImporter implements OutputAlgorithm< Map< Integer, List< Spot > > >
{

	private final String filePath;

	private String errorMessage;

	private final String xColumnName;

	private final String yColumnName;

	private final String zColumnName;

	private final String frameColumnName;

	private final String qualityColumn;

	private final String idColumn;

	private final String nameColumn;

	private final double radius;

	private final double xOrigin;

	private final double yOrigin;

	private final double zOrigin;

	private Map< Integer, List< Spot > > spots;

	public CSVImporter( final String filePath,
			final double radius,
			final String xColumnName, final String yColumnName, final String zColumnName, final String frameColumnName,
			final String qualityColumn, final String nameColumn, final String idColumn,
			final double xOrigin, final double yOrigin, final double zOrigin)
	{
		this.filePath = filePath;
		this.radius = radius;
		this.xColumnName = xColumnName;
		this.yColumnName = yColumnName;
		this.zColumnName = zColumnName;
		this.frameColumnName = frameColumnName;
		this.qualityColumn = qualityColumn;
		this.nameColumn = nameColumn;
		this.idColumn = idColumn;
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.zOrigin = zOrigin;
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
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
			errorMessage = e.getMessage();
			return false;
		}

		try
		{
			final CSVFormat csvFormat = CSVFormat
					.EXCEL
					.withHeader()
					.withCommentMarker( '#' );
			records = csvFormat.parse( in );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
			return false;
		}

		final Map< String, Integer > headerMap = records.getHeaderMap();

		/*
		 * Parse mandatory headers.
		 */

		final Integer xcol = headerMap.get( xColumnName );
		if ( null == xcol )
		{
			errorMessage = "Could not find X column in " + filePath + ". Was looking for " + xColumnName + ".";
			return false;
		}

		final Integer ycol = headerMap.get( yColumnName );
		if ( null == ycol )
		{
			errorMessage = "Could not find Y column in " + filePath + ". Was looking for " + yColumnName + ".";
			return false;
		}

		final Integer zcol = headerMap.get( zColumnName );
		if ( null == zcol )
		{
			errorMessage = "Could not find Z column in " + filePath + ". Was looking for " + zColumnName + ".";
			return false;
		}

		final Integer framecol = headerMap.get( frameColumnName );
		if ( null == framecol )
		{
			errorMessage = "Could not find frame column in " + filePath + ". Was looking for " + frameColumnName + ".";
			return false;
		}

		/*
		 * Parse optional headers.
		 */

		Integer qualitycol = null;
		if ( null != qualityColumn && !qualityColumn.isEmpty() )
			qualitycol = headerMap.get( qualityColumn );

		Integer namecol = null;
		if ( null != nameColumn && !nameColumn.isEmpty() )
			namecol = headerMap.get( nameColumn );

		Integer idcol = null;
		if ( null != idColumn && !idColumn.isEmpty() )
			idcol = headerMap.get( idColumn );

		/*
		 * Prepare spot collection.
		 */

		spots = new HashMap<>();

		/*
		 * Iterate over records.
		 */

		for ( final CSVRecord record : records )
		{
			try
			{
				final double x = Double.parseDouble( record.get( xcol ) ) + xOrigin;
				final double y = Double.parseDouble( record.get( ycol ) ) + yOrigin;
				final double z = Double.parseDouble( record.get( zcol ) ) + zOrigin;
				// 1-based to 0-based.
				final int t = Integer.parseInt( record.get( framecol ) ) - 1;

				double q = 1.;
				if ( null != qualitycol )
					q = Double.parseDouble( record.get( qualitycol ) );

				String name = null;
				if ( null != namecol )
					name = record.get( namecol );

				final Spot spot;
				if (null != idcol)
				{
					// Hijack spot IDs: we force ID to match ID provided.
					final int id = Integer.parseInt(  record.get( idcol ) );
					spot = new Spot( id );
					spot.putFeature( Spot.POSITION_X, x );
					spot.putFeature( Spot.POSITION_Y, y );
					spot.putFeature( Spot.POSITION_Z, z );
					spot.putFeature( Spot.FRAME, ( double ) t );
					spot.putFeature( Spot.QUALITY, q );
					spot.putFeature( Spot.RADIUS, radius );
					spot.setName( name );
				}
				else
				{
					spot = new Spot( x, y, z, radius, q, name );
				}

				List< Spot > list = spots.get( Integer.valueOf( t ) );
				if ( null == list )
				{
					list = new ArrayList<>();
					spots.put( Integer.valueOf( t ), list );
				}
				list.add( spot );

			}
			catch ( final NumberFormatException nfe )
			{
				nfe.printStackTrace();
				System.out.println( "Could not parse line " + record.getRecordNumber() + ". Malformed number, skipping.\n" + nfe.getMessage() );
				continue;
			}

		}

		/*
		 * Return.
		 */

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public Map< Integer, List< Spot > > getResult()
	{
		return spots;
	}

	/*
	 * MAIN METHOD
	 */

	public static void main( final String[] args )
	{
		final String filePath = "samples/DH1_670_100x_1_49_Microtubules_001-1.30x30_DHlocalization.csv";

		final CSVImporter importer = new CSVImporter(
				filePath, 30,
				"x [nm]", "y [nm]", "z [nm]", "frame",
				"photons", null, null,
				0., 0., 0.);

		if ( !importer.checkInput() || !importer.process() )
		{
			System.out.println( importer.getErrorMessage() );
			return;
		}

		final Map< Integer, List< Spot > > spots = importer.getResult();
		System.out.println( spots.toString() );

	}

}
