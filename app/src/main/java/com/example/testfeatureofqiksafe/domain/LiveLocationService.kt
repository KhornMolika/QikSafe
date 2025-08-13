// domain/LiveLocationService.kt
package com.example.testfeatureofqiksafe.domain

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.google.android.gms.location.*

class LiveLocationService : Service() {

    private lateinit var client: FusedLocationProviderClient
    private lateinit var request: LocationRequest
    private lateinit var callback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        createChannel()

        client = LocationServices.getFusedLocationProviderClient(this)
        request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(0)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                LiveLocationBus.emit(result)
            }
        }

        startForeground(1001, buildNotification())

        // ✅ Guard & try/catch to satisfy lint + runtime safety
        if (!hasAnyLocationPermission()) {
            // No runtime permission; stop the service gracefully
            stopSelf()
            return
        }
        try {
            // We’ve checked permissions, so suppress the lint warning here
            @Suppress("MissingPermission")
            @SuppressLint("MissingPermission")
            client.requestLocationUpdates(request, callback, mainLooper)
        } catch (se: SecurityException) {
            // Permission might still be revoked between check & call
            android.util.Log.e("LiveLocationService", "Missing location permission", se)
            stopSelf()
        }
    }

    private fun hasAnyLocationPermission(): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            client.removeLocationUpdates(callback)
        } catch (_: SecurityException) { /* ignore */ }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "live_location_channel"
        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Sharing live location")
            .setContentText("Your emergency location is being shared.")
            .setOngoing(true)
        return builder.build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "live_location_channel"
            val channel = NotificationChannel(
                channelId, "Live Location", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
