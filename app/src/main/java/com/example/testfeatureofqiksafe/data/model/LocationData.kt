package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class LocationData(
    val locationId: String = "",
    val userId: String = "",
    val timestamp: Timestamp? = null,
    val location: GeoPoint? = null,
    val accuracy: Double = 0.0
)
