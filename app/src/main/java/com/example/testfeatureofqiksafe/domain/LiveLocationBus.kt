package com.example.testfeatureofqiksafe.domain

import com.google.android.gms.location.LocationResult
import java.util.concurrent.CopyOnWriteArrayList

object LiveLocationBus {
    private val listeners = CopyOnWriteArrayList<(LocationResult) -> Unit>()

    fun register(l: (LocationResult) -> Unit) {
        listeners.add(l)
    }

    fun unregister(l: (LocationResult) -> Unit) {
        listeners.remove(l)
    }

    fun unregisterAll() {
        listeners.clear()
    }

    fun emit(result: LocationResult) {
        listeners.forEach { it.invoke(result) }
    }
}