from fiji.plugin.trackmate.exporter.csv import TrackMateExporter
import sys
import argparse

parser = argparse.ArgumentParser(description='Launch the TrackMate CSV exporter.')
parser.add_argument('--csvFilePath', type=str, help='The path to the CSV file to export.', required=True)
parser.add_argument('--imageFilePath', help='The path to the image file.', required=True)
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
parser.add_argument('--declareAllFeatures', help='If true, all available features will be declared in the export.')
args = parser.parse_args()

print(args)

builder = TrackMateExporter.builder()
builder.xCol( args.xCol )
builder.yCol( args.yCol )
builder.frameCol( args.frameCol )

print( builder.create() )