package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class AlertHistory(
    val alertHistoryId: String = "",
    val alertId: String = "", // 🔗 link to the original EmergencyAlert
    val userId: String = "",
    val alertTime: Timestamp = Timestamp.now(),
    val resolvedTime: Timestamp? = null,
    val status: String = "resolved", // "resolved", "cancelled", etc.
    val summary: String? = null,
    val locationSnapshot: GeoPoint = GeoPoint(0.0, 0.0),
    val contactsNotified: List<ContactsNotified> = emptyList(),
    val policeStationNotified: PoliceStationNotified? = null,
    val mediaEvidence: MediaEvidence? = null
)

