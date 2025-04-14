package com.example.osufoottrafficapp.helpers

import android.graphics.Color
import com.example.osufoottrafficapp.model.GeoJsonFeatureCollection
import com.example.osufoottrafficapp.model.GeoJsonGeometry
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class GeoJsonHelper {
    companion object {
        private fun mapTrafficWeightToColor(trafficWeight: Int): String {
            return when (trafficWeight) {
                1 -> "#95d01f" // Yellow-green
                2 -> "#fcc734" // Yellow
                3 -> "#fc9b34" // Orange
                4 -> "#eb3434" // Red
                5 -> "#95171d" // Dark red
                else -> "#00000000" // Default Black
            }
        }

        fun renderGeoJsonTraffic(map: GoogleMap, geoJsonData: GeoJsonFeatureCollection) {
            val filteredFeatures = geoJsonData.getFeaturesAtTime()
            for (feature in filteredFeatures) {
                if (feature.properties.trafficWeight == 0) {
                    continue
                }
                val line = drawLineString(
                    feature.geometry,
                    mapTrafficWeightToColor(feature.properties.trafficWeight)
                )
                map.addPolyline(line)
            }
        }

        private fun drawLineString(geometry: GeoJsonGeometry, lineColor: String): PolylineOptions {
            val coordinates = geometry.coordinates

            val polylineOptions = PolylineOptions().apply {
                color(Color.parseColor(lineColor))
                width(5f)
                geodesic(true)
            }

            for (c in coordinates) {
                val latLng = LatLng(c[1], c[0]) // Flip cause GeoJSON: [longitude, latitude]
                polylineOptions.add(latLng)
            }

            return polylineOptions
        }
    }
}

/*
class GeoJsonHelper {

    companion object {
        fun renderGeoJsonTraffic(map: GoogleMap, geoJsonData: GeoJsonFeatureCollection) {
            for (feature in geoJsonData.features) {
                val line = drawLineString(feature.geometry)
                map.addPolyline(line)
            }
        }

        fun renderGeoJson(map: GoogleMap, geoJsonData: GeoJsonFeatureCollection) : Polyline {
            val myPolylineOptionsInstance = PolylineOptions()
                .width(3f)
                .color(0x7F0000FF)

            for (feature in geoJsonData.features) {
                addLineStringToPolygon(myPolylineOptionsInstance, feature.geometry)
            }

            val line = map.addPolyline(myPolylineOptionsInstance)
            map.add
            return line
        }

        private fun drawLineString(geometry: GeoJsonGeometry) : PolylineOptions{
            val coordinates = geometry.coordinates

            val polylineOptions = PolylineOptions().apply {
                color(0x7F0000FF)
                width(5f)
                geodesic(true)
            }

            for (c in coordinates) {
                val latLng = LatLng(c[1], c[0]) // Flip cause GeoJSON: [longitude, latitude]
                polylineOptions.add(latLng)
            }

            return polylineOptions
        }


        private fun addLineStringToPolygon(poly: PolylineOptions, geometry: GeoJsonGeometry) {
            val coordinates = geometry.coordinates

            for (element in coordinates) {
                val start = LatLng(element[1], element[0])
                poly.add(start)
            }
        }
    }
}*/