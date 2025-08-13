package com.example.testfeatureofqiksafe.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlin.math.max

class PowerTriplePressDetector(
    private val context: Context,
    private val windowMs: Long = 1_500L,  // time window to count 3 toggles
    private val onTriggered: () -> Unit
) {

    private var receiver: BroadcastReceiver? = null
    private val presses = ArrayDeque<Long>()

    fun start() {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                if (action == Intent.ACTION_SCREEN_OFF || action == Intent.ACTION_SCREEN_ON) {
                    val now = System.currentTimeMillis()
                    presses.addLast(now)
                    // drop old
                    while (presses.isNotEmpty() && now - presses.first() > windowMs) {
                        presses.removeFirst()
                    }
                    if (presses.size >= 3) {
                        presses.clear()
                        onTriggered()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        context.registerReceiver(receiver, filter)
    }

    fun stop() {
        receiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        receiver = null
        presses.clear()
    }
}
