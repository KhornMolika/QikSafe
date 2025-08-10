package com.example.testfeatureofqiksafe.data.model

data class UserSettings(
    val userId: String = "",
    val locationSharing: Boolean = true,
    val alertTone: String = "default",
    val notificationsEnabled: Boolean = true,
    val shakeToAlertEnabled: Boolean = true,
    val powerButtonAlertEnabled: Boolean = true,
    val callAlertenabled: Boolean = false,
    val preferredLanguage: String = "en"
)
