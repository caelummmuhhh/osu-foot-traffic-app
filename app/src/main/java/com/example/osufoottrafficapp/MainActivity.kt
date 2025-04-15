package com.example.osufoottrafficapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.osufoottrafficapp.databinding.ActivityMainBinding
import com.example.osufoottrafficapp.ui.fragment.MapFragment
import com.example.osufoottrafficapp.ui.fragment.MarkersFragment
import com.example.osufoottrafficapp.ui.fragment.SettingsFragment

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity()/*, OnMapReadyCallback*/ {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapButton.setOnClickListener(::mapButtonOnClickListener)
        binding.markersButton.setOnClickListener(::markersButtonOnClickListener)
        binding.settingsButton.setOnClickListener(::settingsButtonOnClickListener)


        if (savedInstanceState == null) {
            val mapFragment = MapFragment()
            val markersFragment = MarkersFragment()
            val settingsFragment = SettingsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.main_fragment_container, mapFragment, "MAP_FRAGMENT")
                .add(R.id.main_fragment_container, markersFragment, "MARKERS_FRAGMENT")
                .add(R.id.main_fragment_container, settingsFragment, "SETTINGS_FRAGMENT")
                .hide(markersFragment)
                .hide(settingsFragment)
                .commit()

        }

        setActiveButton(R.id.map_button)
    }

    private fun mapButtonOnClickListener(view: View) {
        val mapFragment = supportFragmentManager.findFragmentByTag("MAP_FRAGMENT") ?: MapFragment()
        showFragment(mapFragment)
        setActiveButton(R.id.map_button)
    }

    private fun markersButtonOnClickListener(view: View) {
        val markersFragment = supportFragmentManager.findFragmentByTag("MARKERS_FRAGMENT") ?: MarkersFragment()
        showFragment(markersFragment)
        setActiveButton(R.id.markers_button)
    }
    private fun settingsButtonOnClickListener(view: View) {
        val settingsFragment = supportFragmentManager.findFragmentByTag("SETTINGS_FRAGMENT") ?: SettingsFragment()
        showFragment(settingsFragment)
        setActiveButton(R.id.settings_button)
    }

    private fun setActiveButton(activeButtonId: Int) {
        val buttons = listOf(binding.mapButton, binding.markersButton, binding.settingsButton)
        for (button in buttons) {
            val colorRes = if (button.id == activeButtonId) {
                R.color.scarlet_dark_40
            } else {
                R.color.dark_gray_60
            }
            button.setColorFilter(
                ContextCompat.getColor(this, colorRes),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        // Hide all fragments first
        supportFragmentManager.fragments.forEach { transaction.hide(it) }

        if (fragment.isAdded) {
            transaction.show(fragment) // If it's already added, just show it
        } else {
            transaction.add(R.id.main_fragment_container, fragment) // Add if not already added
        }

        transaction.commit()
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }
}