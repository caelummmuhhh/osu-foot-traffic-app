package com.example.osufoottrafficapp.model

class GeoJsonSimplifiedFeatureCollection(
    val type: String,
    val name: String,
    val features: List<GeoJsonSimplifiedFeature>
) {
    constructor(
        featCollection: GeoJsonFeatureCollection,
        simplifiedFeats: List<GeoJsonSimplifiedFeature>
    ) : this(
        featCollection.type,
        featCollection.name,
        simplifiedFeats
    )
}
