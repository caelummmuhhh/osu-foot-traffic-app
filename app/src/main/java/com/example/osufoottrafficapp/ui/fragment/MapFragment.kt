package com.example.osufoottrafficapp.ui.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.osufoottrafficapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private var googleMap: GoogleMap? = null  // Use a nullable variable
    private lateinit var markerDatabase: MarkerDatabase
    private lateinit var markerDao: MarkerDao

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        //Load Markers in database
        loadMarkers()

        // Set a click listener to add markers dynamically
        googleMap?.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "MapFragment onCreateView() called!")
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //Init DB and DAO
        markerDatabase = MarkerDatabase.getDatabase(requireContext())
        markerDao = markerDatabase.markerDao()
    }

    //Adds marker when clicked AND save to db
    private fun addMarker(latLng: LatLng) {
        //Add marker
        val marker = googleMap?.addMarker(
                MarkerOptions().position(latLng).title("Marker at ${latLng.latitude}, ${latLng.longitude}")
            )
        Log.d(TAG, "Marker at ${latLng.latitude}, ${latLng.longitude} created!")
        //Save marker
        marker?.let {
            saveMarkerToDatabase(latLng)
        }
        Log.d(TAG, "Marker at ${latLng.latitude}, ${latLng.longitude} saved!")
    }
    private fun saveMarkerToDatabase(latLng: LatLng) {
        val markerEntity = MarkerEntity(latitude = latLng.latitude, longitude = latLng.longitude, title = "Custom Marker")

        // Use a coroutine to insert data asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            markerDao.insertMarker(markerEntity)
        }
    }
    private fun loadMarkers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val markers = markerDao.getAllMarkers()

            withContext(Dispatchers.Main) {
                for (marker in markers) {
                    googleMap?.addMarker(
                        MarkerOptions().position(LatLng(marker.latitude, marker.longitude)).title(marker.title)
                    )
                }
            }
        }
    }
}
