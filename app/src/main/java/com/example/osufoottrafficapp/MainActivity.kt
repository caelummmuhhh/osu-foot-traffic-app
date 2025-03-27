package com.example.osufoottrafficapp

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.example.osufoottrafficapp.databinding.ActivityMainBinding
import com.example.osufoottrafficapp.ui.fragment.MapFragment
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
        binding.settingsButton.setOnClickListener(::settingsButtonOnClickListener)

        if (savedInstanceState == null) {
            val mapFragment = MapFragment()
            val settingsFragment = SettingsFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.main_fragment_container, mapFragment, "MAP_FRAGMENT")
                .add(R.id.main_fragment_container, settingsFragment, "SETTINGS_FRAGMENT")
                .hide(settingsFragment) // Start with settings hidden
                .commit()
        }
    }

    private fun mapButtonOnClickListener(view: View) {
        val mapFragment = supportFragmentManager.findFragmentByTag("MAP_FRAGMENT") ?: MapFragment()
        showFragment(mapFragment)
    }

    private fun settingsButtonOnClickListener(view: View) {
        val settingsFragment = supportFragmentManager.findFragmentByTag("SETTINGS_FRAGMENT") ?: SettingsFragment()
        showFragment(settingsFragment)
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