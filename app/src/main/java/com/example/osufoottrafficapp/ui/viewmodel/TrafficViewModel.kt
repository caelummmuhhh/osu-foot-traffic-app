package com.example.osufoottrafficapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.osufoottrafficapp.data.FirebaseStorageHelper
import com.example.osufoottrafficapp.data.FirebaseStoragePath
import com.example.osufoottrafficapp.model.GeoJsonFeatureCollection
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class TrafficViewModel : ViewModel() {
    var geoJsonObject: GeoJsonFeatureCollection? = null
    private var storageHelper: FirebaseStorageHelper = FirebaseStorageHelper()


    suspend fun getTrafficModel(): GeoJsonFeatureCollection {
        if (geoJsonObject == null) {
            geoJsonObject = downloadAndParseJson() // your expensive function
        }
        return geoJsonObject!!
    }

    private suspend fun downloadAndParseJson(): GeoJsonFeatureCollection? =
        withContext(Dispatchers.IO) {  // Run on background thread
            try {
                val geoJsonRef =
                    storageHelper.getReference(FirebaseStoragePath.TRAFFIC_DATA_GEOJSON)
                val inputStream = geoJsonRef.stream.await().stream

                /* Read using buffered reader */
                val reader = BufferedReader(
                    InputStreamReader(inputStream, Charsets.UTF_8),
                    16 * 1024
                ) // 16 KB
                return@withContext Gson().fromJson(reader, GeoJsonFeatureCollection::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }
}
