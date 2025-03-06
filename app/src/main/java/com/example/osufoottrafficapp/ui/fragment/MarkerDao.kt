package com.example.osufoottrafficapp.ui.fragment

import androidx.room.*

@Dao
interface MarkerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers")
    suspend fun getAllMarkers(): List<MarkerEntity>

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)

    @Query("DELETE FROM markers")
    fun deleteAllMarkers()

    //This function is still broken - work on passing paramaters to delete single marker
    @Query("DELETE FROM markers WHERE id = :markerId")
    suspend fun deleteMarkerById(markerId: Int)

    @Update
    fun updateMarker(marker: MarkerEntity)
}
