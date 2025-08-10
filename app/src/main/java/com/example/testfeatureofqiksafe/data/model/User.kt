package com.example.testfeatureofqiksafe.data.model
import com.google.firebase.firestore.GeoPoint

data class User(
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImage: String = "",
    val emergencyContactIds: List<String> = emptyList(),
    val lastKnownLocation: GeoPoint? = null,
    val alertHistoryIds: List<String> = emptyList(),
    val chatHistoryId: String = ""
)
