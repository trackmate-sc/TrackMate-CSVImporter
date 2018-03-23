"""
A Jython script that parse arguments and uses them to configure a CSV to TrackMate 
exporter. 

This script must be called from Fiji (to have everything on the class path), for instance
in headless mode. Here is an example of a call from the command line:

./ImageJ-macosx --headless   ../../../TrackMateCSVImporter/scripts/CsvToTrackMate.py 
	--csvFilePath="../../../TrackMateCSVImporter/samples/data.csv" 
	--imageFilePath="../../../TrackMateCSVImporter/samples/171004-4mins-tracking.tif"
	 --xCol=1 
	 --radius=2 
	 --yCol=2 
	 --zCol=3 
	 --frameCol=0
	 --targetFilePath="../../../TrackMateCSVImporter/samples/data.xml"
"""

from fiji.plugin.trackmate.exporter.csv import TrackMateExporter
from java.io import File
import argparse


def main(): 
	parser = argparse.ArgumentParser(description='Launch the TrackMate CSV exporter.')
	parser.add_argument('--csvFilePath', type=str, help='The path to the CSV file to export.', required=True)
	parser.add_argument('--imageFilePath', type=str, help='The path to the image file.', required=True)
	parser.add_argument('--targetFilePath', type=str, help='The path to the TrackMate xml file to create.', required=True)
	parser.add_argument('--xCol', type=int, help='The column where the spot X positions are listed (0-based).', required=True)
	parser.add_argument('--yCol', type=int, help='The column where the spot Y positions are listed (0-based).', required=True)
	parser.add_argument('--zCol', type=int, help='The column where the spot Z positions are listed (0-based).')
	parser.add_argument('--frameCol', type=int, help='The column where the spot frames are listed (0-based).', required=True)
	parser.add_argument('--idCol', type=int, help='The column where the spot IDs are listed (0-based).')
	parser.add_argument('--qualityCol', type=int, help='The column where the spot quality values are listed (0-based).')
	parser.add_argument('--nameCol', type=int, help='The column where the spot names are listed (0-based).')
	parser.add_argument('--trackCol', type=int, help='The column where the spot track indices are listed (0-based).')
	parser.add_argument('--radiusCol', type=int, help='The column where the spot radiuses are listed (0-based).')
	parser.add_argument('--radius', type=float, help='The default radius, to use if a radius column is not available.')
	parser.add_argument('--declareAllFeatures', type=bool, help='If true, all available features will be declared in the export.')
	args = parser.parse_args()

	builder = TrackMateExporter.builder()
	builder.csvFilePath( args.csvFilePath )
	builder.imageFilePath( args.imageFilePath )
	builder.xCol( args.xCol )
	builder.yCol( args.yCol )
	builder.frameCol( args.frameCol )

	if args.zCol is not None:
		builder.zCol( args.zCol )

	if args.idCol is not None:
		builder.idCol( args.idCol )

	if args.qualityCol is not None:
		builder.qualityCol( args.qualityCol )
	
	if args.nameCol is not None:
		builder.nameCol( args.nameCol )	
	
	if args.trackCol is not None:
		builder.trackCol( args.trackCol )	
	
	if args.radiusCol is not None:
		builder.radiusCol( args.radiusCol )	
	
	if args.radius is not None:
		builder.radius( args.radius )
	
	if args.declareAllFeatures is not None:
		builder.declareAllFeatures( args.declareAllFeatures )
	
	if args.radius is None and args.radiusCol is None:
		print( "Please specify at least --radius or --radiusCol" )
		return
	
	exporter = builder.create()
	ok = exporter.exportTo( File( args.targetFilePath ) )
	if not ok:
		print( exporter.getErrorMessage() )


#___________________________
main()
