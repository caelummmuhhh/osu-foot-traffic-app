# How Sidewalk Centerline Geospatial Data is Collected
The OSU GIS Team Sidewalk Center data end point:
    https://gissvc.osu.edu/arcgis/rest/services/Data/ReferenceData_RO/MapServer/9/query  
This data endpoint contains sidewalk/walking path information on OSU properties, including the main and regional campuses, airport, and other facilities.
#### General Information About the GIS Data
- Data is paginated.
- Data return is in Esri JSON, specifically `esriGeometryPolyline`.
- Available in spatial reference EPSG:4326 (the one we use) and 102100/EPSG:3857
- An online ogr2ogr endpoint is used to convert Esri JSON into GeoJSON (http://ogre.adc4gis.com/convert).
- `download_sidewalk_data.py` is used to download the entirety of the sidewalk data into one file.

# How Course Information is Collected
Course information is collected from two sources: the OSU course catalog and barrett.3's course schedule. The OSU course catalog has neat and useful information on a course's times and location, but the enrollment numbers for a course is an aggregate of all sections. This is not useful because each section can have different number of students and locations/times. barrett.3's course schedule has enrollment info for each section, but the location and time data are not neat or clean.

** These processes are done in the `playgtound.ipynb` jupyter notebook. ** 

### How Term Codes are Calculated
Term codes are a 4 digit integer that correspond to the terms (e.g. SP25 = 1252).  
To calculate:  
The first three digits is: calendar year - 1900  
The last digit (one's place): 0=winter (quarters only), 2=spring, 4=summer, 8=autumn  
{calendar year - 1900} & {0/winter, 2/spring, 4/summer, 8/autumn}  
So, SP25 = {2025-1900 = 125} + {2=spring} = 1252

### Collect Data from OSU Course Catalog
End point: https://content.osu.edu/v2/classes/search
- This endpoint is the query for when users search on the Class Search UI.
- Returned data is paginated and returns only 200 sections at a time.
- Each query/link can only return 10,000 sections at a time.
- The best way to retrieve everything is to go through one subject at a time with no search string. That way, each query/link doesn't exceed the 10k limit.
- `osu_subjects.json` contains every subject available at OSU and their "term" for the request parameters.
    - Information in this file is collected from tracing and accessing the variables on the website using Firefox's browser DevTools (inspect element). 

### Collect Data from barrett.3's Schedule
Link: https://www.asc.ohio-state.edu/barrett.3/schedule/
- https://www.asc.ohio-state.edu/barrett.3/schedule/{ SUBJECT }/{ 4-DIGIT TERM CODE }.txt
- Don't know who maintains this, how it updates, or where the info is coming from, but it seems accurate.
- Data available in .txt formatted into columns of data.

