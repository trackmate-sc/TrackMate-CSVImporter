package fiji.plugin.trackmate.exporter.csv;

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
		if (metadataTokens.isEmpty())
			return "No metadata\n";

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
