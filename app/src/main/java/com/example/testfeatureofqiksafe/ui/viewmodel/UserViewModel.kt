package com.example.testfeatureofqiksafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testfeatureofqiksafe.data.model.User
import com.example.testfeatureofqiksafe.data.repository.UserRepository
import com.google.firebase.firestore.ListenerRegistration

class UserViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private var listenerReg: ListenerRegistration? = null

    fun startListeningToUserProfile() {
        listenerReg?.remove() // Remove old listener if already running
        listenerReg = repo.listenToUserProfile { u ->
            _user.postValue(u)
        }
    }

    fun stopListeningToUserProfile() {
        listenerReg?.remove()
        listenerReg = null
    }

    fun updateUserProfile(updated: Map<String, Any>) {
        repo.updateUserProfile(updated) { ok ->
            if (!ok) {
                // Optional error handling
            }
        }
    }

}
