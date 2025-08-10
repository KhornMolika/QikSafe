package com.example.testfeatureofqiksafe.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testfeatureofqiksafe.data.model.Contact
import com.example.testfeatureofqiksafe.data.model.SelectableContact
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _deviceContacts = MutableLiveData<List<SelectableContact>>()
    val deviceContacts: LiveData<List<SelectableContact>> get() = _deviceContacts

    fun startListening(userId: String) {
        Log.d("ContactViewModel", "startListening called with userId=$userId")
        _loading.value = true
        repository.listenToContacts(
            userId,
            onDataChanged = { contactList ->
                Log.d("ContactViewModel", "Contacts received: $contactList")
                _loading.postValue(false)
                _contacts.postValue(contactList)
            },
            onError = { e ->
                Log.e("ContactViewModel", "Error fetching contacts", e)
                _loading.postValue(false)
                _error.postValue(e.message)
            }
        )
    }

    fun stopListening() {
        repository.stopListening()
    }

    fun addContact(contact: Contact) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.addContact(contact)
            _loading.postValue(false)
            result.onFailure { e -> _error.postValue(e.message) }
        }
    }

    fun updateContact(contactId: String, updatedData: Map<String, Any>) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.updateContact(contactId, updatedData)
            _loading.postValue(false)
            result.onFailure { e -> _error.postValue(e.message) }
        }
    }

    fun deleteContact(contactId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.deleteContact(contactId)
            _loading.postValue(false)
            result.onFailure { e -> _error.postValue(e.message) }
        }
    }


    fun loadDeviceContacts(context: Context) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val list = repository.getDeviceContacts(context)
                _deviceContacts.postValue(list)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }

}
