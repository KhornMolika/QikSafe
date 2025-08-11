package com.example.testfeatureofqiksafe.data.repository

import com.example.testfeatureofqiksafe.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserRepository(
    private val firestore: FirebaseFirestore
) {

    private fun uid(): String? = FirebaseAuth.getInstance().uid
    private fun userDoc() = firestore.collection("users").document(uid()!!)

    fun listenToUserProfile(onChange: (User?) -> Unit): ListenerRegistration? {
        val u = uid() ?: return null
        return userDoc().addSnapshotListener { snapshot, error ->
            if (error != null) {
                onChange(null)
                return@addSnapshotListener
            }
            onChange(snapshot?.toObject(User::class.java))
        }
    }

    fun updateUserProfile(updatedData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val u = uid() ?: return onComplete(false)
        userDoc().update(updatedData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}