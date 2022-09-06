[![Build Status](https://github.com/trackmate-sc/TrackMate-CSVImporter/actions/workflows/build.yml/badge.svg)](https://github.com/trackmate-sc/TrackMate-CSVImporter/actions/workflows/build.yml)

# CSV to TrackMate importer. 

The importer can be found in the `Plugins > Tracking > TrackMate CSV importer` menu.

## Using the GUI.

The example below shows a capture of the GUI when re-importing a CSV file created by TrackMate itself (from the `Analysis` button).

![GUI illustration](docs/TrackMateCSVImporter_01.png?raw=true "TrackMate CSV importer GUI")

Open the target image in Fiji, and browse to the CSV file from the GUI. It will be parsed and the parameter lists will be populated with the headers of the CSV file. Some columns are mandatory (X, Y, frame). If you uncheck `Compute all features?` box, only a minimal set of features will be declared and computed.

Depending on whether you specify to import the track values or not, the TrackMate GUI will be created at a different stage. 

## Running the importer from the command line.

You can use Fiji in headless mode, to call the Jython script 
`CsvToTrackMate.py` that will parse arguments and configure the 
importer properly. Here is an example:

``` sh
./ImageJ-macosx --headless   /path/to/scripts/CsvToTrackMate.py 
	--csvFilePath="/path/to/MyCsvFile.csv" 
	--imageFilePath="/path/to/MyImage.tif"
	 --xCol=1 
	 --radius=2 
	 --yCol=2 
	 --zCol=3 
	 --frameCol=0
	 --targetFilePath="/path/to/TrackMateFile.xml"
```

This will create a new TrackMate file `/path/to/TrackMateFile.xml` with detections created from the CSV file `/path/to/MyCsvFile.csv` and reading the image metadata from image file `/path/to/MyImage.tif`.


