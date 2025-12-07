package com.example.zalgneyhmusic.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.model.domain.User
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.session.UserManager
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for Account & Settings Side Sheet
 * Follows MVVM pattern and Clean Architecture principles
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userManager: UserManager
) : BaseViewModel() {

    val currentUser: LiveData<User?> = userManager.currentUser.asLiveData()

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _updateState = MutableLiveData<Resource<User>>()
    val updateState: LiveData<Resource<User>> = _updateState

    init {
        ensureUserSessionLoaded()
    }

    private fun ensureUserSessionLoaded() {
        if (userManager.currentUserValue == null) {
            viewModelScope.launch {
                // Call sync to retrieve user information from Backend (MongoDB) based on Firebase Token
                val result = authRepository.syncUserToBackEnd()

                if (result is Resource.Success) {
                    // Save to RAM for subsequent use
                    userManager.saveUserSession(result.result)
                }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        try {
            authRepository.logout()
            userManager.clearSession()
            _logoutSuccess.value = true
        } catch (_: Exception) {
            _logoutSuccess.value = false
        }
    }

    fun getCurrentUser() = userManager.currentUserValue
    fun getUserDisplayName(): String = userManager.currentUserValue?.displayName ?: "User"

    fun getUserEmail(): String = userManager.currentUserValue?.email ?: "No email"

    fun getUserPhotoUrl(): String? = userManager.currentUserValue?.photoUrl

    fun updateUserProfile(displayName: String, imageFile: File?) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading

            val result = authRepository.updateUserProfile(displayName, imageFile)

            _updateState.value = result
        }
    }

}

