package com.example.osufoottrafficapp.model

import java.time.LocalDateTime

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
