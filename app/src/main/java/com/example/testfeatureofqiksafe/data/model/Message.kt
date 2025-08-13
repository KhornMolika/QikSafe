package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val type: String = "text",          // "text" | "location" | "image" ...
    val text: String? = null,
    val fromUserId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lat: Double? = null,
    val lng: Double? = null,
    val accuracy: Float? = null
)