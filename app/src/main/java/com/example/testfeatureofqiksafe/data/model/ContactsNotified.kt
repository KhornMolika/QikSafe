package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.Timestamp

data class ContactsNotified(
    val contactId: String = "",
    val received: Boolean = false,
    val timeReceived: Timestamp? = null,
)