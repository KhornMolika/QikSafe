package com.example.testfeatureofqiksafe.data.repository

import android.content.Context
import com.example.testfeatureofqiksafe.data.model.User
import com.example.testfeatureofqiksafe.util.SharedPrefHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore
) {

    private fun userDocument(userId: String) = firestore.collection("users").document(userId)

    fun fetchUserProfile(context: Context, onComplete: (User?) -> Unit) {
        val userId = SharedPrefHelper.getUserId(context)
        if (userId.isNullOrEmpty()) {
            onComplete(null)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(User::class.java)
                    onComplete(profile)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun updateUserProfile(context: Context, updatedData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val userId = SharedPrefHelper.getUserId(context)
        if (userId.isNullOrEmpty()) {
            onComplete(false)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(updatedData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Adds a contactId to the user's emergencyContactIds list (no duplicates).
     */
    suspend fun addEmergencyContactId(userId: String, contactId: String) {
        val userDocRef = userDocument(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)
            val existingList = snapshot.get("emergencyContactIds") as? List<String> ?: emptyList()

            if (!existingList.contains(contactId)) {
                val updatedList = existingList + contactId
                transaction.update(userDocRef, "emergencyContactIds", updatedList)
            }
        }.await()
    }

}