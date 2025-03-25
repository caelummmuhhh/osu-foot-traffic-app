package com.example.osufoottrafficapp.ui.fragment

import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.osufoottrafficapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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
import com.google.maps.android.collections.MarkerManager
import org.json.JSONObject
import android.Manifest as Manifest1
//Idk what this import does but it causes an error.
//import com.example.osufoottrafficapp.Manifest as Manifest2

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var markerManager: MarkerManager
    private lateinit var markerCollection: MarkerManager.Collection
    private val storageRef = Firebase.storage.reference
    private var buildingsLayer: GeoJsonLayer? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        // Manage all markers independently from polygons, lines, layers, etc.
        markerManager = MarkerManager(googleMap)
        markerCollection = markerManager.newCollection()

        //Observe LiveData for marker updates
        markerViewModel.allMarkers.observe(viewLifecycleOwner, Observer { markerList ->
            //Clear the map and add the markers from the LiveData list
            markerCollection.clear()
            markerList.forEach { markerEntity ->
                val latLng = LatLng(markerEntity.latitude, markerEntity.longitude)
                val markerOptions = MarkerOptions().position(latLng).title(markerEntity.title)
                markerCollection.addMarker(markerOptions)
            }
        })

        // Set OnMapClickListener to add markers
        googleMap.setOnMapClickListener { latLng ->
            //Call a function to add a new marker at the clicked location
            addMarker(latLng)
        }

        //Add OnMarkerClickListener
        markerCollection.setOnMarkerClickListener { marker ->
            //Handle when a specific marker is clicked
            showMarkerOptionsDialog(marker)
            //Return true if successful handle
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
        checkLocationPermission()
        mapFragment?.getMapAsync(callback)
    }

    private fun createBuildingsLayer() {
        // Get the .geojson file from db and parse it into JsonOBJECT
        val downloadByteLimit: Long = 2 * 1024 * 1024
        val geojsonRef = storageRef.child("geojson/osu_buildings.json")
        geojsonRef.getBytes(downloadByteLimit).addOnSuccessListener { byteArray ->
            val strJson = String(byteArray)
            val jsonData = JSONObject(strJson)

            buildingsLayer = GeoJsonLayer(googleMap, jsonData, markerManager, null, null, null)
            buildingsLayer?.setOnFeatureClickListener { feature ->
                Log.d(
                    TAG,
                    "Building clicked: ${feature.getProperty("BLDG_NAME") ?: "Unknown Building"}"
                )
            }
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
        val marker = markerCollection.addMarker(markerOptions)

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
       val updatedMarker = markerCollection.addMarker( MarkerOptions().position(marker.position).title(newTitle))
        //Save the marker in the database
        updatedMarker?.let {
            val markerEntity = MarkerEntity(
                title = it.title ?: newTitle,
                latitude = it.position.latitude,
                longitude = it.position.longitude
            )
            //Insert the marker into the database
            markerViewModel.insertMarker(markerEntity)
            markerCollection.remove(marker)
        }
    }

    private fun deleteMarker() {
        //Deletes all markers from the database
        markerViewModel.deleteAllMarkers()
        markerCollection.clear()
    }

    //Check for location permissions
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest1.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest1.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest1.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? -> location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Toast.makeText(
                    requireContext(),
                    "Lat: $latitude, Lng: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            } ?: run {
                Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show()
            } }
        }
    }
}
