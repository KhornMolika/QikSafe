package com.example.testfeatureofqiksafe.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testfeatureofqiksafe.data.model.UserSettings
import com.example.testfeatureofqiksafe.data.repository.SettingsRepository
import com.google.firebase.firestore.ListenerRegistration

class SettingsViewModel(private val repo: SettingsRepository): ViewModel() {

    private val _settings = MutableLiveData<UserSettings?>()
    val settings: LiveData<UserSettings?> get() = _settings

    private var listener: ListenerRegistration? = null

    fun startListening() {
        listener?.remove()
        listener = repo.listenToSettings { s -> _settings.postValue(s) }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    // === Functions you asked for ===
    fun saveTriggerPreference(method: String, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        repo.saveTriggerMethod(method, onResult)
    }

    fun saveActionPreference(pref: String, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        repo.saveActionPreference(pref, onResult)
    }

    fun saveEmergencyNumber(number: String, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        repo.saveEmergencyNumber(number, onResult)
    }

    fun saveLocationSharing(enabled: Boolean, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        repo.saveLocationSharing(enabled, onResult)
    }
}