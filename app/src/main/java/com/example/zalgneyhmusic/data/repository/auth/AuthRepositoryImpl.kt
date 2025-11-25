package com.example.zalgneyhmusic.data.repository.auth

import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.model.domain.User
import com.example.zalgneyhmusic.data.model.utils.await
import com.example.zalgneyhmusic.service.ZalgneyhApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

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
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ZalgneyhApiService
) : AuthRepository {

    /**
     * The currently authenticated Firebase user, or null if no user is logged in.
     */
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Synchronizes the currently authenticated Firebase user with the backend server.
     *
     * This process involves:
     * 1. Retrieving the current Firebase user.
     * 2. Getting a fresh ID Token from Firebase.
     * 3. Sending the token to the backend API for verification and user creation/update.
     *
     * @return A [Resource] containing the synchronized [User] domain model if successful,
     * or a [Resource.Failure] if synchronization fails (e.g., no user, network error).
     */
    override suspend fun syncUserToBackEnd(): Resource<User> {
        val user = firebaseAuth.currentUser
        if (user == null) {
            android.util.Log.e("DEBUG_SYNC", "Lỗi: Current User is NULL")
            return Resource.Failure(Exception("No User"))
        }

        return try {
            // Retrieve fresh ID token
            val tokenResult = user.getIdToken(true).await()
            val token = tokenResult.token

            // Send token to backend
            val response = apiService.syncUser("Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Failure(Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

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