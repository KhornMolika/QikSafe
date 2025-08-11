package com.example.testfeatureofqiksafe.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testfeatureofqiksafe.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> get() = _registerResult

    fun registerUser(name: String, phone: String, email: String, password: String) {
        repository.registerUser(name, phone, email, password) { success, error ->
            _registerResult.value = Pair(success, error)
        }
    }

    fun loginWithEmail(
        context: Context,
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        repository.loginWithEmail(context, email, password, onComplete)
    }

    fun logout(context: Context) {
        repository.logout(context)
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

}
