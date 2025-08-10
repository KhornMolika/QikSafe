package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val text: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text", // or "alert"
    val status: String = "",  // e.g., sent, delivered, seen
    val location: GeoPoint? = null // for emergency alerts
)