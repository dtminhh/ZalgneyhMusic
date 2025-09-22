package com.example.zalgneyhmusic.data.model.repository

import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Implementation of [AuthRepository] that uses Firebase Authentication
 * to handle user authentication operations such as login, signup, and logout.
 *
 * This class leverages FirebaseAuth for all authentication-related tasks
 * and wraps the results in a [Resource] to provide success or failure states.
 *
 * @property firebaseAuth The [FirebaseAuth] instance injected by Hilt for performing authentication operations.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    /**
     * The currently authenticated Firebase user, or null if no user is logged in.
     */
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Logs in a user with the given [email] and [password].
     *
     * Uses [FirebaseAuth.signInWithEmailAndPassword] internally.
     *
     * @param email The email address of the user.
     * @param password The password associated with the email.
     * @return A [Resource.Success] containing the logged-in [FirebaseUser] if successful,
     *         or a [Resource.Failure] with the exception if login fails.
     */
    override suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Creates a new user account with the given [email] and [password].
     *
     * Uses [FirebaseAuth.createUserWithEmailAndPassword] internally.
     *
     * @param email The email address for the new account.
     * @param password The password for the new account.
     * @return A [Resource.Success] containing the created [FirebaseUser] if successful,
     *         or a [Resource.Failure] with the exception if signup fails.
     */
    override suspend fun signup(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Signs in a user with Google using the provided ID token.
     *
     * - Creates a Firebase credential from the Google ID token.
     * - Uses Firebase Authentication to sign in with the credential.
     * - Awaits the result asynchronously.
     *
     * @param idToken The ID token retrieved from Google Sign-In flow.
     * @return [Resource] containing:
     *   - [Resource.Success] with the authenticated [FirebaseUser] if sign-in succeeds.
     *   - [Resource.Failure] with the thrown [Exception] if sign-in fails.
     */
    override suspend fun signInWithGoogle(idToken: String): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Logs out the currently authenticated user.
     *
     * Uses [FirebaseAuth.signOut] internally to clear the active session.
     */
    override fun logout() {
        firebaseAuth.signOut()
    }
}