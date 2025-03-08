package com.example.osufoottrafficapp.ui.fragment

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class MarkerRepository(private val markerDao: MarkerDao) {
    val allMarkers: LiveData<List<MarkerEntity>> = markerDao.getAllMarkers()

    suspend fun insertMarker(marker: MarkerEntity) {
        markerDao.insertMarker(marker)
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
