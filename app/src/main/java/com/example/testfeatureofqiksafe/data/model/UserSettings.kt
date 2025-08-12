package com.example.testfeatureofqiksafe.data.model

data class UserSettings(
    val userId: String = "",
    val locationSharing: Boolean = false,
    val triggerMethod: String = "shake",                // "shake" | "power"
    val actionPreference: String = "send_location_call",// "send_location_call" | "send_location_only" | "call_only"
    val emergencyNumber: String = "",
    val alertTone: String = "default",
    val notificationsEnabled: Boolean = true,
    val preferredLanguage: String = "en"
)

