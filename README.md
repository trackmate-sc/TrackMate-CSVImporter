# CSV to TrackMate exporter. 

## Running the exporter from the command line.

You can use Fiji in headless mode, to call the Jython script 
`CsvToTrackMate.py` that will parse arguments and configure the 
exporter properly. Here is an example:

``` sh
./ImageJ-macosx --headless   ../../../TrackMateCSVImporter/scripts/CsvToTrackMate.py 
	--csvFilePath="../../../TrackMateCSVImporter/samples/MyCsvFile.csv" 
	--imageFilePath="../../../TrackMateCSVImporter/samples/MyImage.tif"
	 --xCol=1 
	 --radius=2 
	 --yCol=2 
	 --zCol=3 
	 --frameCol=0
	 --targetFilePath="../../../TrackMateCSVImporter/samples/TrackMateFile.xml"
```



