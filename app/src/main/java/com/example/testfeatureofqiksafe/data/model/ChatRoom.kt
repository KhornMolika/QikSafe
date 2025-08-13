package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp

data class ChatRoom(
    val chatId: String = "",
    val participants: List<String> = emptyList(), // [uidA, uidB] or more for group
    val createdAt: Timestamp = Timestamp.now(),
    val lastMessagePreview: String = "",
    val lastMessageAt: Timestamp? = null
)
