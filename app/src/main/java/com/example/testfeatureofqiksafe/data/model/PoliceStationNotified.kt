package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp

data class PoliceStationNotified(
    val stationId: String = "",
    val received: Boolean = false,
    val timeReceived: Timestamp? = null,
    val distance: Double = 0.0 // in km
)
