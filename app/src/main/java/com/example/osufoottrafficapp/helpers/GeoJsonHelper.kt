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
