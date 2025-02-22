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
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_fragment_container, MapFragment())
                .commit()
        }
    }

    private fun mapButtonOnClickListener(view: View) {
        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment_container)
        if (currentFragment is MapFragment) {
            return
        }
        replaceFragment(MapFragment(), R.id.main_fragment_container)
    }

    private fun settingsButtonOnClickListener(view: View) {
        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment_container)
        if (currentFragment is SettingsFragment) {
            return
        }
        replaceFragment(SettingsFragment(), R.id.main_fragment_container)
    }

    private fun replaceFragment(fragment: Fragment, id: Int) {
        supportFragmentManager
            .beginTransaction()
            .replace(id, fragment)
            .commit()
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