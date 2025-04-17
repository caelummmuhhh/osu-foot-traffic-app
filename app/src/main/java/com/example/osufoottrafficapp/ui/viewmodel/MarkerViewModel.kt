package com.example.osufoottrafficapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.osufoottrafficapp.ui.fragment.MarkerDatabase
import com.example.osufoottrafficapp.ui.fragment.MarkerEntity
import com.example.osufoottrafficapp.ui.fragment.MarkerRepository
import kotlinx.coroutines.launch

class MarkerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MarkerRepository
    val allMarkers: LiveData<List<MarkerEntity>>

    init {
        val markerDao = MarkerDatabase.getDatabase(application).markerDao()
        repository = MarkerRepository(markerDao)
        allMarkers = repository.allMarkers
    }

    fun getMarkerByLocation(
        latitude: Double,
        longitude: Double,
        callback: (MarkerEntity?) -> Unit
    ) {
        viewModelScope.launch {
            val marker = repository.getMarkerByLocation(latitude, longitude)
            callback(marker)
        }
    }

    fun updateMarker(marker: MarkerEntity) {
        viewModelScope.launch {
            repository.updateMarker(marker)
        }
    }


    fun insertMarker(marker: MarkerEntity) = viewModelScope.launch {
        repository.insertMarker(marker)
    }

    fun deleteMarker(marker: MarkerEntity) = viewModelScope.launch {
        repository.deleteMarker(marker)
    }

    // Method to delete all markers
    fun deleteAllMarkers() = viewModelScope.launch {
        repository.deleteAllMarkers()
    }
}

