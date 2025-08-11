package com.example.testfeatureofqiksafe.data.repository

import android.content.Context
import com.example.testfeatureofqiksafe.data.model.User
import com.example.testfeatureofqiksafe.util.SharedPrefHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue

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
     * Remove a contactId of the user's emergencyContactIds list .
     */
    suspend fun removeEmergencyContactId(userId: String, contactId: String) {
        val userDocRef = userDocument(userId)
        firestore.runTransaction { txn ->
            txn.update(userDocRef, "emergencyContactIds", FieldValue.arrayRemove(contactId))
        }.await()
    }

}