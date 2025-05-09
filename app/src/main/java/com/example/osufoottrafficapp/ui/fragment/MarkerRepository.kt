package com.example.osufoottrafficapp.ui.fragment

import androidx.lifecycle.LiveData

class MarkerRepository(private val markerDao: MarkerDao) {
    val allMarkers: LiveData<List<MarkerEntity>> = markerDao.getAllMarkers()

    suspend fun insertMarker(marker: MarkerEntity) {
        markerDao.insertMarker(marker)
    }

    suspend fun getMarkerByLocation(latitude: Double, longitude: Double): MarkerEntity? {
        return markerDao.getMarkerByLocation(latitude, longitude)
    }

    suspend fun updateMarker(marker: MarkerEntity) {
        markerDao.updateMarker(marker)
    }


    suspend fun deleteMarker(marker: MarkerEntity) {
        markerDao.deleteMarker(marker)
    }

    suspend fun deleteAllMarkers() {
        markerDao.deleteAllMarkers()
    }
}
