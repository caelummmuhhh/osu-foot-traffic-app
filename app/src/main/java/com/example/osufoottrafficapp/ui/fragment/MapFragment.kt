package com.example.osufoottrafficapp.ui.fragment

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.osufoottrafficapp.R
import com.example.osufoottrafficapp.data.FirebaseStorageHelper
import com.example.osufoottrafficapp.data.FirebaseStoragePath
import com.example.osufoottrafficapp.helpers.GeoJsonHelper
import com.example.osufoottrafficapp.model.GeoJsonFeatureCollection
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import android.Manifest as Manifest1


//Idk what this import does but it causes an error.
//import com.example.osufoottrafficapp.Manifest as Manifest2

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var markerManager: MarkerManager
    private lateinit var markerCollection: MarkerManager.Collection
    private lateinit var geoJsonObject: GeoJsonFeatureCollection
    private var storageHelper: FirebaseStorageHelper = FirebaseStorageHelper()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentRoute: com.google.android.gms.maps.model.Polyline? = null
    private var userLocationMarker: Marker? = null
    private var trafficCrScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var trafficJob: Job? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        setupMap()
        //createBuildingsLayer()

        getGeoJson { geoJson ->
            if (geoJson != null) {
                //GeoJsonHelper.renderGeoJsonTraffic(googleMap, geoJson)
                geoJsonObject = geoJson
                startTrafficRenderingTimer(googleMap)
            } else {
                Log.e(TAG, "Failed to load geojson.")
            }
        }
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)  // Ensures only one map initialization

        checkLocationPermission()

    }

    override fun onResume() {
        super.onResume()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback) // Ensure the map reloads when coming back
        // Fetch and update user location
        getCurrentLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::googleMap.isInitialized) {
            googleMap.clear()
        }
    }

    private fun startTrafficRenderingTimer(googleMap: GoogleMap) {
        if (trafficJob?.isActive == true) {
            Log.w(TAG, "Traffic rendering timer is already running, skipping reinitialization.")
            return
        }

        trafficJob = trafficCrScope.launch(Dispatchers.Default) { // Background thread
            while (isActive) { // Loop until coroutine is cancelled
                withContext(Dispatchers.Main) { // Switch to main thread for UI update
                    GeoJsonHelper.renderGeoJsonTraffic(googleMap, geoJsonObject)
                }
                Log.e(TAG, "Ran rendering!")
                delay(2 * 60 * 1000L) // Wait 5 minutes
            }
        }
    }

    private suspend fun downloadAndParseJson(): GeoJsonFeatureCollection? =
        withContext(Dispatchers.IO) {  // Run on background thread
            try {
                val geoJsonRef =
                    storageHelper.getReference(FirebaseStoragePath.TRAFFIC_DATA_GEOJSON)
                val inputStream = geoJsonRef.stream.await().stream

                val reader = InputStreamReader(inputStream, Charsets.UTF_8)
                return@withContext Gson().fromJson(
                    reader,
                    GeoJsonFeatureCollection::class.java
                ) // Parse streaming
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun getGeoJson(callback: (GeoJsonFeatureCollection?) -> Unit) {
        storageHelper.downloadFileBytes(FirebaseStoragePath.TRAFFIC_DATA_GEOJSON)
            .addOnSuccessListener { byteArray ->
                val geoJson =
                    Gson().fromJson(String(byteArray), GeoJsonFeatureCollection::class.java)
                callback(geoJson)
            }
            .addOnFailureListener { err ->
                err.printStackTrace()
                callback(null)
            }
    }

    //Add a marker to the map and save it to the database
    fun addMarker(latLng: LatLng) {
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
    fun showMarkerOptionsDialog(marker: Marker) {
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

        builder.setNegativeButton("Delete") { _, _ ->
            deleteMarker(marker) // Delete only this marker
        }

        //Cancel closes the dialogue
        builder.setNeutralButton("Cancel", null)
        builder.show()
    }

    //Called if update button is pressed
    private fun updateMarker(marker: Marker, newTitle: String) {
        // Retrieve the existing marker entity from the ViewModel
        markerViewModel.getMarkerByLocation(marker.position.latitude, marker.position.longitude) { existingMarker ->
            if (existingMarker != null) {
                // If marker exists, update its title and update it in the database
                val updatedMarker = existingMarker.copy(title = newTitle)
                markerViewModel.updateMarker(updatedMarker)

                // Remove the old marker and add the updated one to the map
                markerCollection.remove(marker)
                markerCollection.addMarker(MarkerOptions().position(marker.position).title(newTitle))
            }
        }
    }




    private fun deleteMarker(marker: Marker) {
        // Find and delete the marker from the database
        markerViewModel.getMarkerByLocation(marker.position.latitude, marker.position.longitude) { existingMarker ->
            existingMarker?.let {
                markerViewModel.deleteMarker(it) // Remove from Room database
            }
        }

        // Remove the marker from the map
        markerCollection.remove(marker)

        // Clear the route from the map
        currentRoute?.remove()
        currentRoute = null
    }


    //Check for location permissions
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest1.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest1.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Default
    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest1.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)

                    // Move the camera to the user's location
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    googleMap.isMyLocationEnabled = true
                    googleMap.uiSettings.isMapToolbarEnabled = true
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.uiSettings.isMyLocationButtonEnabled = true

                    if (userLocationMarker == null) {
                        // Add the marker for the first time
                        userLocationMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    } else {
                        // Just update the marker position instead of creating a new one
                        userLocationMarker?.position = userLatLng
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    //Routing
    private fun getCurrentLocation(callback: (LatLng) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest1.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    callback(userLatLng)
                } ?: run {
                    Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupMap() {
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

        googleMap.setOnMapClickListener { latLng -> addMarker(latLng) }

        // Set OnMapClickListener to add markers
        googleMap.setOnMapClickListener { latLng ->
            //Call a function to add a new marker at the clicked location
            addMarker(latLng)
        }

        //Add OnMarkerClickListener
        markerCollection.setOnMarkerClickListener { marker ->
            //Handle when a specific marker is clicked
            showMarkerOptionsDialog(marker)
            getCurrentLocation { userLocation ->
                fetchShortestRoute(userLocation, marker.position)
            }
            //Return true if successful handle
            true
        }

        //createBuildingsLayer()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.999396, -83.012504), 15f))
        getCurrentLocation()
    }

    private fun fetchShortestRoute(start: LatLng, end: LatLng) {
        val apiKey = "AIzaSyCkwfieddAavvjI6oR3FSKkLm9EidSq8F8"
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&mode=walking&key=$apiKey"

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    drawRouteOnMap(points)
                }
            },
            { error ->
                Log.e("MapFragment", "Error fetching route: ${error.message}")
            })

        requestQueue.add(jsonObjectRequest)
    }

    private fun drawRouteOnMap(encodedPolyline: String) {
        // Remove the existing route before drawing a new one
        currentRoute?.remove()

        val polylineOptions = PolylineOptions()
            .color(Color.BLUE)
            .width(8f)
            .addAll(PolyUtil.decode(encodedPolyline))

        currentRoute = googleMap.addPolyline(polylineOptions)
    }


}
