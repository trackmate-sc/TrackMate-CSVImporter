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
package fiji.plugin.trackmate.importer.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSVMetadata
{

	private final Map< String, String > metadataTokens;

	private CSVMetadata( final Map< String, String > metadataTokens )
	{
		this.metadataTokens = metadataTokens;
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		for ( final String field : metadataTokens.keySet() )
		{
			result.append( " - " );
			result.append( field );
			result.append( " = " );
			result.append( metadataTokens.get( field ) );
			result.append( '\n' );
		}
		return result.toString();
	}

	private static class CSVMetadataBuilder
	{

		public CSVMetadata parse( final String filePath ) throws FileNotFoundException, IOException
		{
			final Map< String, String > map = new HashMap<>();
			try (BufferedReader br = new BufferedReader( new FileReader( filePath ) ))
			{
				for ( String line; ( line = br.readLine() ) != null; )
				{
					final String str = line.trim().toLowerCase();
					if ( !str.startsWith( "#" ) )
						break;

					final String[] split = str.split( "," );
					if ( split.length < 2 )
						continue;

					final String key = split[ 0 ].replace( '#', ' ' ).trim();
					final String value = split[ 1 ];
					map.put( key, value );
				}
			}
			finally
			{}

			return new CSVMetadata( map );
		}
	}

	public static CSVMetadata parse( final String filePath ) throws FileNotFoundException, IOException
	{
		return new CSVMetadataBuilder().parse( filePath );
	}
}
