import requests
import json
import os
import shutil

def request_sidewalk_data(out_dir: str) -> list:
    """
    Request sidewalk centerline data from the OSU GIS servers.
    Data will be outputted into a specified directory in a series of Esri JSON files.
    """
    
    MAX_PER_PAGE = 2000 # API is paginated and maximum return record count is 2000.
    gisUrl = 'https://gissvc.osu.edu/arcgis/rest/services/Data/ReferenceData_RO/MapServer/9/query'
    gisParams = {
        'where': '',
        'text': '',
        'objectIds': '',
        'time': '',
        'timeRelation': 'esriTimeRelationOverlaps',
        'geometry': '1061166.374377791%2C1.452705617858285E7%2C1086950.7048244423%2C1.4544208167629968E7',
        'geometryType': 'esriGeometryEnvelope',
        'inSR': '',
        'spatialRel': 'esriSpatialRelIntersects',
        'distance': '',
        'units': 'esriSRUnit_Foot',
        'relationParam': '',
        'outFields': '',
        'returnGeometry': 'true',
        'returnTrueCurves': 'false',
        'maxAllowableOffset': '',
        'geometryPrecision': '',
        'outSR': '4326',
        'havingClause': '',
        'returnIdsOnly': 'false',
        'returnCountOnly': 'false',
        'orderByFields': '',
        'groupByFieldsForStatistics': '',
        'outStatistics': '',
        'returnZ': 'false',
        'returnM': 'false',
        'gdbVersion': '',
        'historicMoment': '',
        'returnDistinctValues': 'false',
        'resultOffset': '0',
        'resultRecordCount': str(MAX_PER_PAGE),
        'returnExtentOnly': 'false',
        'sqlFormat': 'none',
        'datumTransformation': '',
        'parameterValues': '',
        'rangeValues': '',
        'quantizationParameters': '',
        'featureEncoding': 'esriDefault',
        'f': 'pjson'
    }

    fullData = []
    i = 0
    count = 0

    # Keep requesting next n records until results no longer return max per page
    # indicating we've reached the end of the data set. Usually this is around 40k features.
    while i == 0 or len(gisData['features']) == MAX_PER_PAGE:
        gisParams['resultOffset'] = str(i)
        rGis = requests.get(gisUrl, gisParams)

        if (rGis.status_code != 200): 
            print(f'Error at position: {i}')
            continue
        
        gisData = rGis.json()
        fullData.append(gisData)
        with open(os.path.join(out_dir, f'data_{i}.json'), 'w') as wFile:
            json.dump(gisData, wFile)
        i += MAX_PER_PAGE
        count +=len(gisData['features'])
    return fullData


def esri_to_geojson(file_path: str, out_file_path: str):
    """
    Converts Esri JSON from a specified file into GeoJSON using ogr2ogr API.
    Outputs results to specified file and returns it.
    """
    url = 'http://ogre.adc4gis.com/convert'
    params = {
        'sourceSrs': 'EPSG:4326',
        'targetSrs': 'EPSG:3857',
        'forcePlainText': 'false',
        'rfc7946': 'false',
    }
    headers = {'Accept': 'application/json'}
    with open(file_path, 'rb') as esriBitFile:
        files = {'upload': esriBitFile}
        rOgre = requests.post(url, files=files, data=params, headers=headers)
    if rOgre.status_code != 200:
        print(f'Status code {rOgre.status_code}: Failed converting {file_path}')
        return
    gjson = rOgre.json()
    with open(out_file_path, 'w') as geojsonWriteFile:
        json.dump(gjson, geojsonWriteFile)
    
    return gjson


def combine_geojson(dir: str, name: str):
    """
    Combines all GeoJSON files in a directory into one data object
    and returns it.
    """
    geojsonFiles = os.listdir(dir)
    geojsonFeatureCollection = []
    for geojsonFile in geojsonFiles:
        with open(os.path.join(dir, geojsonFile), 'r') as geojsonBitFile:
            geojsonObj = json.load(geojsonBitFile)
        geojsonFeatureCollection.extend(geojsonObj['features'])
    
    return {
        'type': 'FeatureCollection',
        'name': name,
        'features': geojsonFeatureCollection
    }

def make_hashable(item):
    """Convert dictionary into hashable."""
    return (json.dumps(item['attributes'], sort_keys=True), json.dumps(item['geometry'], sort_keys=True))


def main():
    esri_dir = 'esri'
    geojson_dir = 'geojson'

    # Clean out directories if they exist
    for dir in (esri_dir, geojson_dir):
        if os.path.exists(dir):
            shutil.rmtree(dir)
        os.mkdir(dir)

    # Get esri files
    request_sidewalk_data(esri_dir)
    esri_files = os.listdir(esri_dir)

    # Convert esri json to geojson
    for esri_file in esri_files:
        esri_to_geojson(os.path.join(esri_dir, esri_file), os.path.join(geojson_dir, esri_file))

    # Combine and output to a file
    full_data = combine_geojson(geojson_dir, 'OsuSidewalkCenterline')
    with open('sidewalk_centerline.json', 'w') as write_file:
        json.dump(full_data, write_file, indent=4)
    print('Done!')


if __name__ == '__main__':
    main()