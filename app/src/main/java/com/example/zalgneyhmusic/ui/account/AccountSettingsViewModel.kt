package com.example.zalgneyhmusic.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Account & Settings Side Sheet
 * Follows MVVM pattern and Clean Architecture principles
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _currentUser.value = authRepository.currentUser
    }

    fun logout() = viewModelScope.launch {
        try {
            authRepository.logout()
            _logoutSuccess.value = true
        } catch (e: Exception) {
            _logoutSuccess.value = false
        }
    }

    fun getUserDisplayName(): String {
        return authRepository.currentUser?.displayName ?: "User"
    }

    fun getUserEmail(): String {
        return authRepository.currentUser?.email ?: "No email"
    }

    fun getUserPhotoUrl(): String? {
        return authRepository.currentUser?.photoUrl?.toString()
    }
}

