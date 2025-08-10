package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class EmergencyAlert(
    val alertId: String = "",
    val userId: String = "",
    val alertTime: Timestamp? = null,
    val location: GeoPoint? = null,
    val status: String = "sent",
    val contactsNotified: List<ContactsNotified> = emptyList(),
    val policeStationNotified: PoliceStationNotified? = null,
    val mediaEvidence: MediaEvidence? = null,
)

