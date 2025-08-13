package com.example.testfeatureofqiksafe.domain

import android.Manifest
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.testfeatureofqiksafe.data.model.ActionPreference
import com.example.testfeatureofqiksafe.data.model.Contact
import com.example.testfeatureofqiksafe.data.model.UserSettings
import com.example.testfeatureofqiksafe.data.model.toAction
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EmergencyController {

    /** Start emergency flow: choose action(s) based on settings */
    fun startEmergency(
        context: Context,
        settings: UserSettings,
        contactRepo: ContactRepository,
        onStarted: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // 1) Start live location foreground service
        ContextCompat.startForegroundService(
            context,
            Intent(context, LiveLocationService::class.java)
        )
        onStarted()

        // 2) For SEND_LOCATION_* -> stream locations to chatrooms
        when (settings.actionPreference.toAction()) {
            ActionPreference.SEND_LOCATION_CALL,
            ActionPreference.SEND_LOCATION_ONLY -> {
                LiveLocationBus.register { result ->
                    // For each location result, send to chats
                    sendLocationToAllChats(context, settings, result, contactRepo)
                }
            }
            ActionPreference.CALL_ONLY -> {
                // no streaming needed
            }
        }

        // 3) If action requires calling, initiate phone call
        if (settings.actionPreference.toAction() == ActionPreference.SEND_LOCATION_CALL ||
            settings.actionPreference.toAction() == ActionPreference.CALL_ONLY) {
            placeEmergencyCall(context, settings.emergencyNumber)
        }
    }

    /** Stop emergency (service + bus) */
    fun stopEmergency(context: Context) {
        LiveLocationBus.unregisterAll()
        context.stopService(Intent(context, LiveLocationService::class.java))
    }

    /** Send one location batch to all contacts’ chatrooms */
    private fun sendLocationToAllChats(
        context: Context,
        settings: UserSettings,
        locResult: LocationResult,
        contactRepo: ContactRepository
    ) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1) get owner’s contacts
                val contacts: List<Contact> = contactRepo.getContactsForUser(uid)

                // 2) last location (ignore if null)
                val last = locResult.lastLocation ?: return@launch

                // 3) payload
                val payload = mapOf(
                    "type" to "location",
                    "fromUserId" to uid,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "lat" to last.latitude,
                    "lng" to last.longitude,
                    "accuracy" to last.accuracy
                )

                // 4) ensure chat + write message for each contact
                for (c in contacts) {
                    val chatId = ensureChatRoom(db, uid, c.contactId) // <-- contactId from your Contact model
                    db.collection("chatRooms").document(chatId)
                        .collection("messages")
                        .add(payload)
                        .await()
                }
            } catch (e: Exception) {
                // optional: log/handle
                android.util.Log.e("EmergencyController", "sendLocationToAllChats failed", e)
            }
        }
    }

    // Make sure this is suspend and uses await()
    private suspend fun ensureChatRoom(
        db: FirebaseFirestore,
        myUid: String,
        otherId: String
    ): String {
        val a = listOf(myUid, otherId).sorted()
        val chatId = "dm_${a[0]}_${a[1]}"

        val chatRef = db.collection("chatRooms").document(chatId)
        val snap = chatRef.get().await()
        if (!snap.exists()) {
            val data = mapOf(
                "chatId" to chatId,
                "participants" to a,
                "createdAt" to com.google.firebase.Timestamp.now(),
            )
            chatRef.set(data).await()
        }
        return chatId
    }

    /** Place an emergency call (ACTION_CALL if permission granted; else ACTION_DIAL) */
    private fun placeEmergencyCall(context: Context, number: String) {
        val canCall = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CALL_PHONE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (number.isBlank()) {
            Toast.makeText(context, "No emergency number set", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (canCall) {
                val call = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(call)
            } else {
                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                dial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(dial)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to place call: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
