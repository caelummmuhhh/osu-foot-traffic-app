package com.example.osufoottrafficapp.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GeoJsonTrafficModel(
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("weekly_pattern") val weeklyTrafficPattern: Map<String, Map<String, Int>>
) {

    /***
     * Retrieves traffic value (weight) at a specified dateTime.
     *
     * @param dateTime the datetime to get the traffic value for
     * @return the traffic value, if any.
     */
    fun getTrafficValueAtTime(dateTime: LocalDateTime): Int? {
        val flooredDt = floorToNearestFiveMinutes(dateTime)
        val startDateObj = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val endDateObj = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
        val dateObj = flooredDt.toLocalDate()

        // Check if dateTime is within the range (inclusive)
        if ((dateObj.isEqual(startDateObj) || dateObj.isAfter(startDateObj)) &&
            (dateObj.isEqual(endDateObj) || dateObj.isBefore(endDateObj))
        ) {

            val timeStr = flooredDt.format(DateTimeFormatter.ofPattern("kk:mm"))
            val dayOfWeek = flooredDt.dayOfWeek.value % 7 // traffic data calls for 0 = sunday

            return weeklyTrafficPattern[dayOfWeek.toString()]?.get(timeStr)
        }

        return null;
    }

    private fun floorToNearestFiveMinutes(time: LocalDateTime): LocalDateTime {
        val flooredMinutes = (time.minute / 5) * 5 // Floor to nearest multiple of 5
        return time.truncatedTo(ChronoUnit.HOURS).plusMinutes(flooredMinutes.toLong())
    }
}
