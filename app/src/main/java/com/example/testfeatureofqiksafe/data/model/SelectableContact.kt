package com.example.testfeatureofqiksafe.data.model

data class SelectableContact(
    val contactId: String,
    val name: String,
    val phone: String,
    val photoUri: String?,
    var isSelected: Boolean = false
)
