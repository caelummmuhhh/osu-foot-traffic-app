package com.example.osufoottrafficapp.ui.fragment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MarkerEntity::class], version = 2, exportSchema = false)
abstract class MarkerDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao

    companion object {
        @Volatile
        private var INSTANCE: MarkerDatabase? = null

        fun getDatabase(context: Context): MarkerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarkerDatabase::class.java,
                    "marker_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
