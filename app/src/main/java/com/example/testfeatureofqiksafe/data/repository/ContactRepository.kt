package com.example.testfeatureofqiksafe.data.repository

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.example.testfeatureofqiksafe.data.model.Contact
import com.example.testfeatureofqiksafe.data.model.SelectableContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContactRepository(
    private val db: FirebaseFirestore
) {
    private val contactsRef = db.collection("contacts")
    private var snapshotListener: ListenerRegistration? = null

    /**
     * Listen to real-time updates for contacts
     */
    fun listenToContacts(
        userId: String,
        onDataChanged: (List<Contact>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        snapshotListener?.remove() // Remove old listener to avoid duplicates

        snapshotListener = contactsRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val contactList = snapshot?.toObjects(Contact::class.java) ?: emptyList()
                onDataChanged(contactList)
            }
    }

    /**
     * Stop listening to updates
     */
    fun stopListening() {
        snapshotListener?.remove()
        snapshotListener = null
    }

    /**
     * Add a new contact
     */
    suspend fun addContact(contact: Contact): Result<Unit> {
        return try {
            contactsRef.add(contact).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update existing contact
     */
    suspend fun updateContact(contactId: String, updatedData: Map<String, Any>): Result<Unit> {
        return try {
            contactsRef.document(contactId).update(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a contact
     */
    suspend fun deleteContact(contactId: String): Result<Unit> {
        return try {
            contactsRef.document(contactId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviceContacts(context: Context): List<SelectableContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<SelectableContact>()
        val resolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        val seenPhones = mutableSetOf<String>()

        cursor?.use { c ->
            val idIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (c.moveToNext()) {
                val name = c.getString(nameIdx) ?: continue
                val rawPhone = c.getString(numberIdx) ?: continue
                val phone = PhoneNumberUtils.normalizeNumber(rawPhone)
                val id = c.getString(idIdx) ?: continue
                val photo = c.getString(photoIdx)

                if (phone.isNotBlank() && seenPhones.add(phone)) {
                    contacts.add(SelectableContact(id, name, phone, photo))
                }
            }
        }
        contacts
    }



}
