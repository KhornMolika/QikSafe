package com.example.testfeatureofqiksafe.ui.viewmodel

import com.example.testfeatureofqiksafe.data.repository.SettingsRepository

class SettingsViewModelFactory(private val repo: SettingsRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}
