package com.example.zalgneyhmusic.ui.viewmodel.auth

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.repository.AuthRepository
import com.example.zalgneyhmusic.ui.viewmodel.BaseViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for handling user authentication logic.
 *
 * Uses Hilt for dependency injection and communicates with [AuthRepository]
 * to perform authentication operations.
 *
 * Responsibilities:
 * - Manage login and signup flows via [MutableStateFlow] to expose
 *   authentication states (Loading, Success, Failure).
 * - Provide the currently authenticated [FirebaseUser], if available.
 * - Retain state across configuration changes using [androidx.lifecycle.ViewModel].
 *
 * Flows:
 * - [loginFlow]: Observes the state of the login operation.
 * - [signupFlow]: Observes the state of the signup operation.
 *
 * Usage:
 * - Call [login] with email and password to authenticate an existing user.
 * - Call [signup] with email and password to create a new account.
 * - Observe [loginFlow] and [signupFlow] in the UI layer (e.g., Fragment)
 *   to update UI accordingly.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : BaseViewModel() {

    private val _loginFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Resource<FirebaseUser>?> = _loginFlow

    private val _singupFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signupFlow: StateFlow<Resource<FirebaseUser>?> = _singupFlow

    val currentUser: FirebaseUser? get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    /**
     * Initiates login with the given [email] and [password].
     * Updates [loginFlow] with the authentication result.
     */
    fun login(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    /**
     * Initiates sign-up with the given [email] and [password].
     * Updates [signupFlow] with the registration result.
     */
    fun signup(email: String, password: String) = viewModelScope.launch {
        _singupFlow.value = Resource.Loading
        val result = repository.signup(email, password)
        _singupFlow.value = result
    }
}