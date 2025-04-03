package com.example.osufoottrafficapp.ui.fragment

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface MarkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers_table")
    fun getAllMarkers(): LiveData<List<MarkerEntity>> // LiveData to observe markers

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)

    @Query("DELETE FROM markers_table")
    suspend fun deleteAllMarkers()

    @Query("SELECT * FROM markers_table WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun getMarkerByLocation(latitude: Double, longitude: Double): MarkerEntity?

    @Update
    suspend fun updateMarker(marker: MarkerEntity)
}