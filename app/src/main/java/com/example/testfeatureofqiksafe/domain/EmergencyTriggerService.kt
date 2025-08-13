package com.example.testfeatureofqiksafe.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import com.example.testfeatureofqiksafe.data.repository.SettingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmergencyTriggerService : Service() {

    private val presses = ArrayDeque<Long>()
    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1201, buildNotification())

        // Dynamic receiver for screen on/off
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF -> {
                        val now = System.currentTimeMillis()
                        presses.addLast(now)
                        // keep only last ~1.5s
                        while (presses.isNotEmpty() && now - presses.first() > 1500L) {
                            presses.removeFirst()
                        }
                        if (presses.size >= 3) {
                            presses.clear()
                            Log.d("EmergencyTriggerService", "Power triple press detected")
                            onTriplePress()
                        }
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        receiver = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun onTriplePress() {
        // Load current settings and execute action
        CoroutineScope(Dispatchers.IO).launch {
            val repo = SettingsRepository(FirebaseFirestore.getInstance())
            // One-shot fetch via listen + immediate value: reuse your repo if you have a get() helper.
            // For brevity, we’ll listen once:
            var unsub: ListenerRegistration? = null
            unsub = repo.listenToSettings { s ->
                s ?: return@listenToSettings
                unsub?.remove()

                // Only act if action is CALL_ONLY or SEND_LOCATION_CALL, else ignore.
                // (If SEND_LOCATION_ONLY you’d start streaming instead.)
                val contactRepo = ContactRepository(FirebaseFirestore.getInstance())
                EmergencyController.startEmergency(
                    context = this@EmergencyTriggerService,
                    settings = s,
                    contactRepo = contactRepo
                )
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "trigger_guard",
                "Emergency Trigger Guard",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "trigger_guard")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Emergency trigger armed")
            .setContentText("Triple-press power to activate.")
            .setOngoing(true)
            .build()
    }
}