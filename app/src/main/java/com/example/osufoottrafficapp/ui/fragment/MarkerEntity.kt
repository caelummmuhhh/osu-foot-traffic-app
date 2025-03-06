package com.example.osufoottrafficapp.ui.fragment

import androidx.room.Entity
import androidx.room.PrimaryKey

//Database is called markers
@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val latitude: Double,
    val longitude: Double
)