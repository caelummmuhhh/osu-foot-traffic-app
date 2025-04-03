package com.example.osufoottrafficapp.model

class GeoJsonSimplifiedFeature(
    var type: String,
    var properties: GeoJsonSimplifiedProperties,
    var geometry: GeoJsonGeometry
) {
    constructor(feature: GeoJsonFeature, trafficWeight: Int) : this(
        feature.type,
        GeoJsonSimplifiedProperties(trafficWeight),
        feature.geometry
    )
}
