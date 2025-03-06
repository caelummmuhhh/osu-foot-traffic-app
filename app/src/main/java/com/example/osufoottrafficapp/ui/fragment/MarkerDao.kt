package com.example.osufoottrafficapp.ui.fragment

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface MarkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers")
    fun getAllMarkers(): LiveData<List<MarkerEntity>> // LiveData to observe markers

    @Delete
    suspend fun deleteMarker(marker: MarkerEntity)

    @Query("DELETE FROM markers")
    suspend fun deleteAllMarkers()

    @Update
    suspend fun updateMarker(marker: MarkerEntity)
}