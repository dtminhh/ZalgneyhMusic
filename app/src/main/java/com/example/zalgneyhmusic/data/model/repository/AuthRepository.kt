package com.example.zalgneyhmusic.data.model.repository

import com.example.zalgneyhmusic.data.Resource
import com.google.firebase.auth.FirebaseUser

/**
 * Repository interface for handling user authentication.
 * Provides methods for login, signup, and logout using Firebase Authentication.
 */
interface AuthRepository {
    /**
     * The currently authenticated Firebase user, or null if no user is logged in.
     */
    val currentUser: FirebaseUser?

    /**
     * Logs in a user with the given [email] and [password].
     *
     * @param email The email address of the user.
     * @param password The password associated with the email.
     * @return A [Resource] object containing the [FirebaseUser] if successful,
     *         or an error message if the login fails.
     */
    suspend fun login(email: String, password: String): Resource<FirebaseUser>

    /**
     * Registers a new user with the given [email] and [password].
     *
     * @param email The email address of the new user.
     * @param password The password for the new account.
     * @return A [Resource] object containing the created [FirebaseUser] if successful,
     *         or an error message if the signup fails.
     */
    suspend fun signup(email: String, password: String): Resource<FirebaseUser>

    /**
     * Logs out the currently authenticated user.
     */
    fun logout()
}