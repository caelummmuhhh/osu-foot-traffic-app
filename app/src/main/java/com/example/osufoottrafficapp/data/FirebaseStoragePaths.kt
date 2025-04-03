package com.example.osufoottrafficapp.data

enum class FirebaseStoragePath(val path: String) {
    OSU_BUILDINGS_GEOJSON("geojson/osu_buildings.json"),
    TRAFFIC_DATA_GEOJSON("geojson/normalized_cleaned_traffic_data.json");

    override fun toString(): String = path
}