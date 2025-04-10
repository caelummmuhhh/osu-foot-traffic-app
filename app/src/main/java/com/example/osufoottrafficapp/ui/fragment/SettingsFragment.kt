package com.example.osufoottrafficapp.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.osufoottrafficapp.R

class SettingsFragment : Fragment() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        sharedPrefs = requireContext().getSharedPreferences("MapColorPrefs", Context.MODE_PRIVATE)

        setupRadioGroup(
            view.findViewById(R.id.locationColorRadioGroup),
            sharedPrefs.getString(KEY_LOCATION_COLOR, "blue")!!,
            KEY_LOCATION_COLOR
        )
        setupRadioGroup(
            view.findViewById(R.id.colorRadioGroupRoute),
            sharedPrefs.getString(KEY_ROUTE_COLOR, "red")!!,
            KEY_ROUTE_COLOR
        )
        setupRadioGroup(
            view.findViewById(R.id.colorRadioGroupMarker),
            sharedPrefs.getString(KEY_MARKER_COLOR, "green")!!,
            KEY_MARKER_COLOR
        )

        return view
    }

    private fun setupRadioGroup(radioGroup: RadioGroup, currentValue: String, key: String) {
        // Select the saved radio button
        for (i in 0 until radioGroup.childCount) {
            val rb = radioGroup.getChildAt(i) as RadioButton
            if (rb.text.toString().equals(currentValue, ignoreCase = true)) {
                rb.isChecked = true
                break
            }
        }

        // Save selected value on change
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)
            sharedPrefs.edit().putString(key, selected.text.toString().lowercase()).apply()
        }
    }

    companion object {
        const val PREFS_NAME = "MapColorPrefs"
        const val KEY_LOCATION_COLOR = "location_color"
        const val KEY_ROUTE_COLOR = "route_color"
        const val KEY_MARKER_COLOR = "marker_color"
    }

}
