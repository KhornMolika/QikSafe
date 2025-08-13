package com.example.testfeatureofqiksafe.data.repository

import com.example.testfeatureofqiksafe.data.model.ChatRoom
import com.example.testfeatureofqiksafe.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore
) {
    private fun uid(): String = FirebaseAuth.getInstance().uid ?: ""

    private val rooms = db.collection("chatRooms")

    /** Deterministic DM chat id: sorted two ids */
    private fun dmId(a: String, b: String): String {
        val s = listOf(a, b).sorted()
        return "dm_${s[0]}_${s[1]}"
    }

    /** Ensure room exists; returns chatId */
    suspend fun ensureDmRoom(otherUserId: String): String {
        val me = uid()
        val chatId = dmId(me, otherUserId)
        val ref = rooms.document(chatId)
        val snap = ref.get().await()
        if (!snap.exists()) {
            val room = ChatRoom(
                chatId = chatId,
                participants = listOf(me, otherUserId),
                createdAt = Timestamp.now()
            )
            ref.set(room).await()
        }
        return chatId
    }

    /** Send a text message */
    suspend fun sendText(chatId: String, text: String) {
        val msgRef = rooms.document(chatId).collection("messages").document()
        val msg = Message(
            messageId = msgRef.id,
            chatId = chatId,
            type = "text",
            text = text,
            fromUserId = uid(),
            createdAt = Timestamp.now()
        )
        msgRef.set(msg).await()

        // update room preview
        rooms.document(chatId).update(
            mapOf(
                "lastMessagePreview" to text.take(60),
                "lastMessageAt" to Timestamp.now()
            )
        )
    }

    /** Send a location message */
    suspend fun sendLocation(chatId: String, lat: Double, lng: Double, accuracy: Float?) {
        val msgRef = rooms.document(chatId).collection("messages").document()
        val msg = Message(
            messageId = msgRef.id,
            chatId = chatId,
            type = "location",
            text = "Shared a location",
            fromUserId = uid(),
            createdAt = Timestamp.now(),
            lat = lat, lng = lng, accuracy = accuracy
        )
        msgRef.set(msg).await()

        rooms.document(chatId).update(
            mapOf(
                "lastMessagePreview" to "📍 Location",
                "lastMessageAt" to Timestamp.now()
            )
        )
    }

    /** Real-time stream of latest N messages (ascending by time) */
    fun listenToMessages(
        chatId: String,
        limit: Long = 100,
        onChange: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return rooms.document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, err ->
                if (err != null) { onError(err); return@addSnapshotListener }
                val list = snap?.toObjects(Message::class.java).orEmpty().reversed()
                onChange(list)
            }
    }
}