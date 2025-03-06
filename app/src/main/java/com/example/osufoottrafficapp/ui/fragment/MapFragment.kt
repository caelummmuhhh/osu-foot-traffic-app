package com.example.osufoottrafficapp.ui.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.osufoottrafficapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private var googleMap: GoogleMap? = null  // Use a nullable variable

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        // Move the camera to a default location (Sydney for now)
        val sydney = LatLng(-34.0, 151.0)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))

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
    }

    private fun addMarker(latLng: LatLng) {
        googleMap?.let { map ->
            map.addMarker(
                MarkerOptions().position(latLng).title("Marker at ${latLng.latitude}, ${latLng.longitude}")
            )
        } ?: Log.e(TAG, "GoogleMap is not initialized yet!")
    }
}
