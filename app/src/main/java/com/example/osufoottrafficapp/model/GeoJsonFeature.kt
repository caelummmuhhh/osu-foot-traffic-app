package com.example.osufoottrafficapp.model

import java.time.LocalDateTime

class GeoJsonFeature(
    val type: String,
    val properties: GeoJsonProperties,
    val geometry: GeoJsonGeometry
) {
    fun trafficFeatureAtTime(dateTime: LocalDateTime): GeoJsonSimplifiedFeature? {
        val trafficValue = properties.trafficModel.getTrafficValueAtTime(dateTime);
        return if (trafficValue !== null) {
            GeoJsonSimplifiedFeature(this, trafficValue)
        } else {
            null
        }
    }
}
