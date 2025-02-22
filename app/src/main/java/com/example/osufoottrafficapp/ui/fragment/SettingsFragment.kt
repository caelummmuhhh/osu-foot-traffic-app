package com.example.osufoottrafficapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.example.osufoottrafficapp.R

class SettingsFragment : PreferenceFragmentCompat() {
    private var tag: String? = this::class.simpleName
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "Creating SettingsFragment via SettingsFragment()")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        Log.d(tag, "Destroying SettingsFragment via onDestroyView()")
        super.onDestroyView()
    }
}