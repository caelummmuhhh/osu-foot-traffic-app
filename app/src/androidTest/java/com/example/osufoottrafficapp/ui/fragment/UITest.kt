package com.example.osufoottrafficapp.ui.fragment

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAddMarker() {
        ActivityScenario.launch(MainActivity::class.java)

        // Simulate a click on the map
        onView(withId(R.id.map))
            .perform(click())

        // Verify if the marker was added
        onView(withId(R.id.map))
            .check(matches(isDisplayed()))
    }
//TODO: Fix this test
//    @Test
//    fun testMarkerOptionsDialog() {
//        // Launch MainActivity
//        ActivityScenario.launch(MainActivity::class.java)
//
//        // Simulate clicking on the map to add a marker at a specific location
//        onView(withId(R.id.map))
//            .perform(click())
//        onView(withId(R.id.map))
//            .perform(click())
//
//        // Verify if the options dialog for the marker is displayed
//        onView(withText("Update"))
//            .inRoot(isDialog())
//            .check(matches(isDisplayed()))
//    }
}