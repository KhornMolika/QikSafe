package com.example.testfeatureofqiksafe.data.model

data class Contact(
    val contactId: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val photoUri: String? = null,
    val type: String = "", // e.g., "phone that added in emergency setting for calling feature"
    val lastMessage: String = "",
    val unreadCount: Int = 0,

    // NEW: this contact is also an app user, store their UID here
    val peerUid: String? = null
)
