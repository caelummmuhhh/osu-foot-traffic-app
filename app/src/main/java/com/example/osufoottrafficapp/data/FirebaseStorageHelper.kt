package com.example.osufoottrafficapp.data

import com.example.osufoottrafficapp.model.GeoJsonFeatureCollection
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class FirebaseStorageHelper {
    private val storageRef = Firebase.storage.reference

    fun getReference(path: FirebaseStoragePath): StorageReference {
        return storageRef.child(path.path)
    }

    fun downloadFileBytes(path: FirebaseStoragePath): Task<ByteArray> {
        val geoJsonRef = getReference(path)
        return geoJsonRef.getBytes(SIZE_LIMIT)
    }

    companion object {
        private const val SIZE_LIMIT: Long = 1024 * 1024 * 8 // 8 MB limit
    }
}