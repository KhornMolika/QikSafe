package com.example.testfeatureofqiksafe.data.repository

import com.example.testfeatureofqiksafe.data.model.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class SettingsRepository(
    private val firestore: FirebaseFirestore
) {
    private fun uid(): String? = FirebaseAuth.getInstance().uid
    private fun settingsDoc() = firestore.collection("userSettings").document(uid()!!)

    /** Live updates from userSettings/{uid} */
    fun listenToSettings(onChange: (UserSettings?) -> Unit): ListenerRegistration? {
        val u = uid() ?: return null
        return settingsDoc().addSnapshotListener { snap, err ->
            if (err != null || snap == null || !snap.exists()) {
                onChange(null); return@addSnapshotListener
            }
            val s = snap.toObject(UserSettings::class.java)?.copy(userId = u)
            onChange(s)
        }
    }

    /** Upsert any fields you pass */
    fun updateSettings(fields: Map<String, Any>, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        val u = uid() ?: return onResult(false, "Not signed in")
        // ensure userId field exists (handy for debugging/queries)
        val merged = mapOf("userId" to u) + fields
        settingsDoc().set(merged, SetOptions.merge())
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun saveLocationSharing(enabled: Boolean, onResult: (Boolean, String?) -> Unit = {_,_->}) =
        updateSettings(mapOf("locationSharing" to enabled), onResult)

    fun saveTriggerMethod(method: String, onResult: (Boolean, String?) -> Unit = {_,_->}) =
        updateSettings(mapOf("triggerMethod" to method), onResult)

    fun saveActionPreference(pref: String, onResult: (Boolean, String?) -> Unit = {_,_->}) =
        updateSettings(mapOf("actionPreference" to pref), onResult)

    fun saveEmergencyNumber(number: String, onResult: (Boolean, String?) -> Unit = {_,_->}) =
        updateSettings(mapOf("emergencyNumber" to number), onResult)
}
