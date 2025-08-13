package com.example.testfeatureofqiksafe.domain

import android.content.Context
import com.example.testfeatureofqiksafe.data.model.UserSettings
import com.example.testfeatureofqiksafe.data.model.toTrigger
import com.example.testfeatureofqiksafe.data.model.TriggerMethod
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyTriggerManager(private val context: Context) {

    private var powerDetector: PowerTriplePressDetector? = null
    // private var shakeDetector: ShakeDetector? = null

    fun arm(settings: UserSettings) {
        when (settings.triggerMethod.toTrigger()) {
            TriggerMethod.POWER -> {
                if (powerDetector == null) {
                    powerDetector = PowerTriplePressDetector(context) {
                        EmergencyController.startEmergency(
                            context = context,
                            settings = settings,
                            contactRepo = ContactRepository(FirebaseFirestore.getInstance())
                        )
                    }
                }
                powerDetector?.start()
            }
            TriggerMethod.SHAKE -> {
//                if (shakeDetector == null) {
//                    shakeDetector = ShakeDetector(context) {
//                        EmergencyController.startEmergency(
//                            context = context,
//                            settings = settings,
//                            contactRepo = ContactRepository(FirebaseFirestore.getInstance())
//                        )
//                    }
//                }
//                shakeDetector?.start()
            }
        }
    }

    fun disarm() {
        powerDetector?.stop()
//        shakeDetector?.stop()
    }
}
