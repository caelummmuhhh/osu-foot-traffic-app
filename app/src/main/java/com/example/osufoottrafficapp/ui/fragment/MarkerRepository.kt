package com.example.osufoottrafficapp.ui.fragment

import androidx.lifecycle.LiveData

class MarkerRepository(private val markerDao: MarkerDao) {

    // Get all markers wrapped in LiveData
    fun getAllMarkers(): LiveData<List<MarkerEntity>> {
        return markerDao.getAllMarkers()
    }

    suspend fun insertMarker(marker: MarkerEntity) {
        markerDao.insertMarker(marker)
    }

    suspend fun updateMarker(marker: MarkerEntity) {
        markerDao.updateMarker(marker)
    }

    suspend fun deleteAllMarkers() {
        markerDao.deleteAllMarkers()
    }
}
