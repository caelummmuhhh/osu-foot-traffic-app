package com.example.osufoottrafficapp.model

import java.time.LocalDateTime

class GeoJsonFeatureCollection(
    val type: String,
    val name: String,
    val features: List<GeoJsonFeature>
) {
    fun getFeaturesAtTime(dateTime: LocalDateTime = LocalDateTime.now()) : List<GeoJsonSimplifiedFeature> {
        return features.mapNotNull { feat ->
            feat.trafficFeatureAtTime(dateTime)
        }
    }

    fun getCollectionAtTime(dateTime: LocalDateTime = LocalDateTime.now()) : GeoJsonSimplifiedFeatureCollection {
        return GeoJsonSimplifiedFeatureCollection(this, getFeaturesAtTime(dateTime))
    }
}
