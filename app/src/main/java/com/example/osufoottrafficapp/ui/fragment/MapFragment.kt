package com.example.osufoottrafficapp.ui.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.osufoottrafficapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import org.json.JSONObject

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private val storageRef = Firebase.storage.reference
    private var buildingsLayer: GeoJsonLayer? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        //Observe LiveData for marker updates
        markerViewModel.allMarkers.observe(viewLifecycleOwner, Observer { markerList ->
            //Clear the map and add the markers from the LiveData list
            googleMap.clear()
            markerList.forEach { markerEntity ->
                val latLng = LatLng(markerEntity.latitude, markerEntity.longitude)
                val markerOptions = MarkerOptions().position(latLng).title(markerEntity.title)
                googleMap.addMarker(markerOptions)
            }
        })

        // Set OnMapClickListener to add markers
        googleMap.setOnMapClickListener { latLng ->
            //Call a function to add a new marker at the clicked location
            addMarker(latLng)
        }

        //Add OnMarkerClickListener
        googleMap.setOnMarkerClickListener { marker ->
            //Handle when a specific marker is clicked
            showMarkerOptionsDialog(marker)
            //Return true if successfull handle
            true
        }

        createBuildingsLayer()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng( 39.999396, -83.012504), 15f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "MapFragment onCreateView() called!")
        //Get view model
        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Get Fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun createBuildingsLayer() {
        // Get the .geojson file from db and parse it into JsonOBJECT
        val downloadByteLimit: Long = 2 * 1024 * 1024
        val geojsonRef = storageRef.child("geojson/osu_buildings.json")
        geojsonRef.getBytes(downloadByteLimit).addOnSuccessListener { byteArray ->
            val strJson = String(byteArray)
            val jsonData = JSONObject(strJson)

            buildingsLayer = GeoJsonLayer(googleMap, jsonData)
            buildingsLayer?.addLayerToMap()
        }.addOnFailureListener { err ->
            Log.e(TAG, "Error retrieving/parsing osu_buildings.geojson from database.")
            err.printStackTrace()
        }
    }

    //Add a marker to the map and save it to the database
    private fun addMarker(latLng: LatLng) {
        //Create a new marker with a default title
        val markerOptions = MarkerOptions().position(latLng).title("New Marker")

        //Add the marker to the map
        val marker = googleMap.addMarker(markerOptions)

        //Save the marker in the database
        marker?.let {
            val markerEntity = MarkerEntity(
                title = it.title ?: "Unnamed Marker",
                latitude = it.position.latitude,
                longitude = it.position.longitude
            )
            //Insert the marker into the database
            markerViewModel.insertMarker(markerEntity)
        }
    }
    //Display a dialog when a marker is clicked
    private fun showMarkerOptionsDialog(marker: Marker) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Marker Name")

        //EditText allows the user to input a new title
        val input = EditText(requireContext())
        input.setText(marker.title)
        builder.setView(input)

        //Update button saves the input to title if present
        builder.setPositiveButton("Update") { _, _ ->
            val newTitle = input.text.toString()
            if (newTitle.isNotBlank()) {
                updateMarker(marker, newTitle)
            }
        }

        //Delete will remove all Markers
        builder.setNegativeButton("Delete All Markers") { _, _ ->
            deleteMarker()
        }

        //Cancel closes the dialogue
        builder.setNeutralButton("Cancel", null)
        builder.show()
    }
    //Called if update button is pressed
    private fun updateMarker(marker: Marker, newTitle: String) {
       val updatedMarker = googleMap.addMarker( MarkerOptions().position(marker.position).title(newTitle))
        //Save the marker in the database
        updatedMarker?.let {
            val markerEntity = MarkerEntity(
                title = it.title ?: newTitle,
                latitude = it.position.latitude,
                longitude = it.position.longitude
            )
            //Insert the marker into the database
            markerViewModel.insertMarker(markerEntity)
            marker.remove()
        }
    }

    private fun deleteMarker() {
        //Deletes all markers from the database
        markerViewModel.deleteAllMarkers()
    }

}
