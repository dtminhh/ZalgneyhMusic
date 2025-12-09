package com.example.zalgneyhmusic.ui.viewmodel.auth

import androidx.lifecycle.viewModelScope
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.model.domain.User
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.session.UserManager
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
    private val repository: AuthRepository,
    userManager: UserManager
) : BaseViewModel() {

    val currentUserFlow: StateFlow<User?> = userManager.currentUser
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
     * Initiates login with the given [email] and [android.R.attr.password].
     *
     * If Firebase authentication is successful, it attempts to synchronize the user
     * with the backend. The [loginFlow] is updated with the final result.
     *
     * @param email The user's email.
     * @param pass The user's password.
     */
    fun login(email: String, pass: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading

        // 1. Authenticate with Firebase
        val loginResult = repository.login(email, pass)

        if (loginResult is Resource.Success) {
            // 2. Sync with Backend upon successful login
            val syncResult =
                repository.syncUserToBackEnd()

            if (syncResult is Resource.Success) {
                // Sync successful: Update flow with success state
                // Note: Consider mapping syncResult (User) to expected flow type if needed
                _loginFlow.value = loginResult
            } else {
                // Login successful but Sync failed
                _loginFlow.value =
                    Resource.Failure(Exception("Login successful but backend sync failed"))
            }
        } else {
            _loginFlow.value = loginResult
        }
    }

    private val _googleSignInFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val googleSignInFlow: StateFlow<Resource<FirebaseUser>?> = _googleSignInFlow

    /**
     * Authenticates using Google Sign-In and synchronizes the user with the backend.
     *
     * @param idToken The Google ID token.
     */
    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _googleSignInFlow.value = Resource.Loading
        val firebaseResult = repository.signInWithGoogle(idToken)

        if (firebaseResult is Resource.Success) {
            // Sync with Backend
            val syncResult = repository.syncUserToBackEnd()

            if (syncResult is Resource.Success) {
                _googleSignInFlow.value = firebaseResult
            } else {
                _googleSignInFlow.value =
                    Resource.Failure(Exception("Google Sign-In successful but backend sync failed"))
            }
        } else {
            _googleSignInFlow.value = firebaseResult
        }
    }

    /**
     * Registers a new user and synchronizes them with the backend.
     *
     * @param email The user's email.
     * @param password The user's password.
     */
    fun signup(email: String, password: String) = viewModelScope.launch {
        _singupFlow.value = Resource.Loading
        val firebaseResult = repository.signup(email, password)

        if (firebaseResult is Resource.Success) {
            // Sync with Backend
            val syncResult = repository.syncUserToBackEnd()

            if (syncResult is Resource.Success) {
                _singupFlow.value = Resource.Success(firebaseResult.result)
            } else {
                _singupFlow.value =
                    Resource.Failure(Exception("Signup successful but backend sync failed"))
            }
        } else {
            _singupFlow.value = firebaseResult
        }
    }

    /**
     * Resets the Google Sign-In flow state.
     *
     * Sets the `_googleSignInFlow` LiveData/StateFlow to `null`
     * so that observers no longer receive previous Google login results.
     */
    fun resetGoogleSignInFlow() {
        _googleSignInFlow.value = null
    }

    /**
     * Resets the login flow state.
     *
     * Sets the `_loginFlow` LiveData/StateFlow to `null`
     * to clear any previous login result or error message.
     */
    fun resetLoginFlow() {
        _loginFlow.value = null
    }
}