package com.example.osufoottrafficapp.model

import com.google.android.gms.maps.model.LatLng

class GeoJsonGeometry(
    val type: String,
    val coordinates: List<List<Double>>
) {
    fun asLineString(): List<LatLng>? {
        return if (type == "LineString") {
            coordinates.map { LatLng(it[1], it[0]) }
        } else {
            null
        }
    }
}
