package com.example.testfeatureofqiksafe.data.model

enum class TriggerMethod { SHAKE, POWER }
enum class ActionPreference { SEND_LOCATION_CALL, SEND_LOCATION_ONLY, CALL_ONLY }

fun String.toTrigger() = if (this == "power") TriggerMethod.POWER else TriggerMethod.SHAKE
fun String.toAction() = when (this) {
    "send_location_only" -> ActionPreference.SEND_LOCATION_ONLY
    "call_only" -> ActionPreference.CALL_ONLY
    else -> ActionPreference.SEND_LOCATION_CALL
}

