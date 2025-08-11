package com.example.testfeatureofqiksafe.data.repository

import android.content.Context
import com.example.testfeatureofqiksafe.data.model.User
import com.example.testfeatureofqiksafe.util.SharedPrefHelper
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(
        name: String,
        phone: String,
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val currentUser = auth.currentUser
                val uid = currentUser?.uid ?: return@addOnSuccessListener

                // ✅ Send email verification
                currentUser.sendEmailVerification()
                    .addOnSuccessListener {
                        // Continue after sending email
                        val user = User(
                            userId = uid,
                            name = name,
                            phone = phone,
                            email = email,
                            profileImage = "",
                            emergencyContactIds = listOf(),
                            lastKnownLocation = null,
                            alertHistoryIds = listOf(),
                            chatHistoryId = ""
                        )

                        firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                onComplete(true, "Registered successfully. Please verify your email before logging in.")
                            }
                            .addOnFailureListener { err ->
                                onComplete(false, "Failed to save user info: ${err.message}")
                            }
                    }
                    .addOnFailureListener { err ->
                        onComplete(false, "Email verification failed: ${err.message}")
                    }
            }
            .addOnFailureListener { exception ->
                val message = when (exception) {
                    is FirebaseAuthUserCollisionException -> "This email is already registered."
                    is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email address format."
                    else -> exception.localizedMessage ?: "Registration failed."
                }
                onComplete(false, message)
            }
    }

    fun loginWithEmail(
        context: Context,
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                // ✅ Ensure email is verified before logging in
                if (user != null && user.isEmailVerified) {

                    // ✅ Save UID in SharedPreferences
                    SharedPrefHelper.saveUserId(context, user.uid)

                    onComplete(true, null)
                } else {
                    auth.signOut()
                    onComplete(false, "Please verify your email address before logging in.")
                }
            }
            .addOnFailureListener {
                onComplete(false, it.message)
            }
    }

    fun logout(context: Context) {
        auth.signOut()
        SharedPrefHelper.clearAll(context)
    }

}
