package com.example.osufoottrafficapp.model

import com.google.gson.annotations.SerializedName

class GeoJsonProperties(
    val dateUpdate: String,
    @SerializedName("traffic_model") val trafficModel: GeoJsonTrafficModel
) {
}