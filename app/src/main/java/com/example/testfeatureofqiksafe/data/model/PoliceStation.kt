package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.firestore.GeoPoint

data class PoliceStation(
    val stationId: String = "",
    val name: String = "",
    val phone: String = "",
    val location: GeoPoint? = null // Combine lat/lon into GeoPoint
)
