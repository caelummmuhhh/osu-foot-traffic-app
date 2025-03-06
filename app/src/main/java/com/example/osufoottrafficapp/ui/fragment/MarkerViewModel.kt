package com.example.osufoottrafficapp.ui.fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MarkerViewModel(application: Application) : AndroidViewModel(application) {
    private val markerDao: MarkerDao = MarkerDatabase.getDatabase(application).markerDao()
    private val markerRepository = MarkerRepository(markerDao)

    // Exposing LiveData to observe markers
    val allMarkers: LiveData<List<MarkerEntity>> = markerRepository.getAllMarkers()

    // Method to update the marker
    fun updateMarker(marker: MarkerEntity) {
        viewModelScope.launch {
            markerRepository.updateMarker(marker)
        }
    }

    fun insertMarker(marker: MarkerEntity) {
        viewModelScope.launch{
            markerRepository.insertMarker(marker)
        }
    }

    // Method to delete all markers
    fun deleteAllMarkers() {
        viewModelScope.launch {
            markerRepository.deleteAllMarkers()
        }
    }
}

