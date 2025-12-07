package com.example.zalgneyhmusic.data.repository.auth

import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.model.domain.User
import com.google.firebase.auth.FirebaseUser
import java.io.File

/**
 * Repository interface for handling user authentication.
 * Provides methods for login, signup, and logout using Firebase Authentication.
 */
interface AuthRepository {
    /**
     * The currently authenticated Firebase user, or null if no user is logged in.
     */
    val currentUser: FirebaseUser?

    suspend fun updateUserProfile(displayName: String?, imageFile: File?): Resource<User>

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
     * Authenticates a user with Firebase using a Google ID token.
     *
     * This is a suspend function and must be called from a coroutine or another
     * suspend function. It exchanges the provided Google ID token for Firebase
     * credentials, and signs the user into Firebase Authentication.
     *
     * @param idToken The Google ID token obtained from the Google Sign-In flow.
     * @return [Resource] wrapping the result:
     *   - [Resource.Success] containing the authenticated [FirebaseUser] if sign-in is successful.
     *   - [Resource.Failure] containing the exception if an error occurs during sign-in.
     */
    suspend fun signInWithGoogle(idToken: String): Resource<FirebaseUser>


    /**
     * Synchronizes the authenticated Firebase user with the backend database.
     *
     * This method should be called after a successful Firebase login/signup to ensure
     * the user exists in the backend system (MongoDB) and to retrieve backend-specific
     * user details (e.g., database ID, role).
     *
     * @return A [Resource] containing the synchronized [User] domain model if successful,
     * or a [Resource.Failure] if the synchronization fails.
     */
    suspend fun syncUserToBackEnd(): Resource<User>

    /**
     * Logs out the currently authenticated user.
     */
    fun logout()
}