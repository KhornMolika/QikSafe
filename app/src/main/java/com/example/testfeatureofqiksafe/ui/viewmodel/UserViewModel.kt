package com.example.testfeatureofqiksafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testfeatureofqiksafe.data.model.User
import com.example.testfeatureofqiksafe.data.repository.UserRepository

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> get() = _userProfile

    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> get() = _updateStatus

    fun fetchUserProfile(context: android.content.Context) {
        repository.fetchUserProfile(context) { user ->
            _userProfile.postValue(user)
        }
    }

    fun updateUserProfile(context: android.content.Context, updatedData: Map<String, Any>) {
        repository.updateUserProfile(context, updatedData) { success ->
            _updateStatus.postValue(success)
            if (success) fetchUserProfile(context)
        }
    }

}
