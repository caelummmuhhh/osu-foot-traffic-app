package com.example.osufoottrafficapp.ui.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.osufoottrafficapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
        // Add click listener for markers
        googleMap?.setOnMarkerClickListener { marker ->
            showEditDeleteDialog(marker)
            true
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
    private fun showEditDeleteDialog(marker: Marker) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit or Delete Marker")

        val input = EditText(requireContext())
        input.hint = "Enter new marker title"
        input.setText(marker.title)
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newTitle = input.text.toString()
            updateMarkerInDatabase(marker, newTitle)
        }

        builder.setNegativeButton("Delete") { _, _ ->
            deleteMarkerFromDatabase(marker)
        }

        builder.setNeutralButton("Cancel", null)

        builder.show()
    }
    private fun updateMarkerInDatabase(marker: Marker, newTitle: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Perform the database query to get the corresponding MarkerEntity
            val markerEntity = markerDao.getAllMarkers().find {
                it.latitude == marker.position.latitude && it.longitude == marker.position.longitude
            }

            markerEntity?.let {
                // Update the MarkerEntity in the database
                val updatedMarker = it.copy(title = newTitle)
                markerDao.updateMarker(updatedMarker)

                // Ensure we update the marker UI on the main thread
                withContext(Dispatchers.Main) {
                    marker.title = newTitle // Update the marker's title on the map
                    marker.showInfoWindow() // Optional: Refresh info window with new title
                }
            }
        }
    }

    private fun deleteMarkerFromDatabase(marker: Marker) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Delete all markers from the database
            markerDao.deleteAllMarkers()

            // Perform the UI operation on the main thread to remove all markers from the map
            requireActivity().runOnUiThread {
                googleMap?.clear() // Removes all markers from the map
            }
        }
    }
}
