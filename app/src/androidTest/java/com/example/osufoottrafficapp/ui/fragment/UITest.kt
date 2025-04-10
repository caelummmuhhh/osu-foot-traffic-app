package com.example.osufoottrafficapp.ui.fragment

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.osufoottrafficapp.MainActivity
import com.example.osufoottrafficapp.R
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UITest {
//RUN EACH TEST SEPERATElY
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testMap() {
        ActivityScenario.launch(MainActivity::class.java)

        // Simulate a click on the map
        onView(withId(R.id.map))
            .perform(click())

        // Verify the map
        onView(withId(R.id.map))
            .check(matches(isDisplayed()))
    }
//RUN EACH TEST SEPERATELY
    @Test
    fun testAddingMarker() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            // Get the MapFragment
            val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment

            // Ensure the map is properly initialized asynchronously
            mapFragment?.getMapAsync { googleMap ->
                // Perform all map-related actions inside this block
                val markerManager = MarkerManager(googleMap)
                val markerCollection = markerManager.newCollection()

                // Simulate a map click to add a marker at a specific location
                val testLatLng = LatLng(0.0, 0.0)

                // Call the addMarker method from the fragment directly
                mapFragment?.childFragmentManager?.fragments?.firstOrNull()?.let { fragment ->
                    if (fragment is MapFragment) {
                        fragment.addMarker(testLatLng)
                    }
                }

                // Verify if the marker has been added to the markerCollection
                val markerFound = markerCollection.markers.any { it.position == testLatLng }

                // Assert that the marker is found
                assertTrue("Marker should be added at clicked position", markerFound)
            }
        }
    }

    //Check for dialog
    @Test
    fun testEditMarker() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            // Get the MapFragment
            val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment

            // Ensure the map is properly initialized asynchronously
            mapFragment?.getMapAsync { googleMap ->
                // Simulate adding a marker at a specific location
                val testLatLng = LatLng(0.0, 0.0)

                // Simulate adding the marker directly to the Map
                val marker = googleMap.addMarker(MarkerOptions().position(testLatLng).title("Test Marker"))

                // Set the OnMarkerClickListener to simulate a marker click and trigger the dialog
                googleMap.setOnMarkerClickListener { clickedMarker ->
                    if (clickedMarker.position == testLatLng) {
                        // Trigger the dialog
                        (activity.supportFragmentManager.findFragmentById(R.id.map) as? MapFragment)?.showMarkerOptionsDialog(clickedMarker)
                    }
                    true
                }

                // Simulate the click on the marker by manually invoking the listener
                googleMap.setOnMarkerClickListener { clickedMarker ->
                    // Manually trigger the dialog for this specific marker
                    (activity.supportFragmentManager.findFragmentById(R.id.map) as? MapFragment)?.showMarkerOptionsDialog(marker!!)
                    true
                }

                // Verify if the AlertDialog displays
                onView(withText("Update")).check(matches(isDisplayed()))
            }
        }
    }

}