package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp

data class ChatRoom(
    val chatId: String = "",
    val participants: List<String> = listOf(), // user IDs
    val lastMessage: String = "",
    val lastUpdated: Timestamp = Timestamp.now()
)
