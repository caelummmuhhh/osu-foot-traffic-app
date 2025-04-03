package com.example.osufoottrafficapp.helpers

import android.graphics.Color
import com.example.osufoottrafficapp.model.GeoJsonFeatureCollection
import com.example.osufoottrafficapp.model.GeoJsonGeometry
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import com.google.android.gms.maps.model.*
import com.google.maps.android.data.LineString
import java.time.LocalDateTime

class GeoJsonHelper {
    companion object {
        fun render(map: GoogleMap, geojson: GeoJsonFeatureCollection) {
            val bounds = map.projection.visibleRegion.latLngBounds
            val zoomLevel = map.cameraPosition.zoom

            // Set simplification tolerance based on zoom level
            val tolerance = when {
                zoomLevel < 10 -> 0.01  // High simplification (less detail)
                zoomLevel < 15 -> 0.005 // Medium simplification
                else -> 0.001           // Low simplification (more detail)
            }

            // Clear old polylines before rendering new ones
            map.clear()

            // Filter features that are within the visible bounds
            val filteredFeatures = geojson.features.filter { feature ->
                val geometry = feature.geometry
                geometry.asLineString()?.any { bounds.contains(it) } ?: false
            }

            // Draw filtered and simplified polylines
            for (feature in filteredFeatures) {
                val lineString = feature.geometry.asLineString() ?: continue

                val simplifiedPoints = simplifyLineString(lineString, tolerance)

                val polylineOptions = PolylineOptions().apply {
                    addAll(simplifiedPoints)
                    width(5f)
                    color(Color.RED)
                }

                map.addPolyline(polylineOptions)
            }
        }

        // Simplifies a LineString using Douglas-Peucker algorithm
        private fun simplifyLineString(coordinates: List<LatLng>, tolerance: Double): List<LatLng> {
            if (coordinates.size < 3) return coordinates

            val first = coordinates.first()
            val last = coordinates.last()

            val maxDistance = coordinates.subList(1, coordinates.size - 1).maxOfOrNull { point ->
                perpendicularDistance(point, first, last)
            } ?: 0.0

            return if (maxDistance > tolerance) {
                val left =
                    simplifyLineString(coordinates.subList(0, coordinates.size / 2), tolerance)
                val right = simplifyLineString(
                    coordinates.subList(coordinates.size / 2, coordinates.size),
                    tolerance
                )
                left + right.drop(1) // Merge lists, avoiding duplicate points
            } else {
                listOf(first, last)
            }
        }

        // Calculate perpendicular distance for Douglas-Peucker simplification
        private fun perpendicularDistance(point: LatLng, start: LatLng, end: LatLng): Double {
            val dx = end.longitude - start.longitude
            val dy = end.latitude - start.latitude
            val lengthSq = dx * dx + dy * dy

            val t = if (lengthSq != 0.0) {
                ((point.longitude - start.longitude) * dx + (point.latitude - start.latitude) * dy) / lengthSq
            } else {
                0.0
            }

            val projection = LatLng(
                start.latitude + t * dy,
                start.longitude + t * dx
            )

            return distanceBetween(point, projection)
        }

        // Calculate the distance between two LatLng points
        private fun distanceBetween(p1: LatLng, p2: LatLng): Double {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(p2.latitude - p1.latitude)
            val dLng = Math.toRadians(p2.longitude - p1.longitude)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return earthRadius * c
        }


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

        fun renderGeoJson(map: GoogleMap, geoJsonData: GeoJsonFeatureCollection): Polyline {
            val myPolylineOptionsInstance = PolylineOptions()
                .width(3f)
                .color(0x7F0000FF)

            for (feature in geoJsonData.features) {
                addLineStringToPolygon(myPolylineOptionsInstance, feature.geometry)
            }

            val line = map.addPolyline(myPolylineOptionsInstance)
            return line
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

        private fun addLineStringToPolygon(poly: PolylineOptions, geometry: GeoJsonGeometry) {
            val coordinates = geometry.coordinates

            for (element in coordinates) {
                val start = LatLng(element[1], element[0])
                poly.add(start)
            }
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