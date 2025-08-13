package com.example.testfeatureofqiksafe.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testfeatureofqiksafe.data.model.Message
import com.example.testfeatureofqiksafe.data.repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repo: ChatRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private var reg: ListenerRegistration? = null

    fun start(chatId: String) {
        stop()
        reg = repo.listenToMessages(
            chatId,
            onChange = { _messages.postValue(it) },
            onError = { /* TODO show error */ }
        )
    }

    fun stop() {
        reg?.remove()
        reg = null
    }

    fun sendText(chatId: String, text: String) {
        viewModelScope.launch {
            repo.sendText(chatId, text)
        }
    }

    fun sendLocation(chatId: String, lat: Double, lng: Double, acc: Float?) {
        viewModelScope.launch {
            repo.sendLocation(chatId, lat, lng, acc)
        }
    }
}