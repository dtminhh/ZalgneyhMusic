package com.example.zalgneyhmusic.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.zalgneyhmusic.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A helper class that encapsulates Google Sign-In logic using
 * the new Credential Manager API.
 *
 * This class is annotated with `@Singleton`, so Hilt will only
 * provide one instance throughout the application's lifecycle.
 *
 * Responsibilities:
 * - Perform Google Sign-In and return a [GoogleIdTokenCredential].
 * - Handle common sign-in errors (no account, credential error).
 * - Provide a sign-out function (can be extended for clearing cached credentials).
 */
@Singleton
class GoogleSignInHelper @Inject constructor() {

    /**
     * Performs Google Sign-In using Credential Manager.
     *
     * @param context the [Context] required to access the Credential Manager API.
     * @return [Result] containing:
     *   - [GoogleIdTokenCredential] if sign-in is successful.
     *   - Failure with Exception if sign-in fails.
     *
     * Error cases handled:
     * - [NoCredentialException]: No Google account available on device,
     *   returns failure with "NO_GOOGLE_ACCOUNT".
     * - [GetCredentialException]: Credential retrieval failed, returns failure with
     *   "CREDENTIAL_ERROR: <error message>".
     * - Any other [Exception]: Wrapped and returned as failure.
     */
    suspend fun signIn(context: Context): Result<GoogleIdTokenCredential> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.success(credential)
        } catch (_: NoCredentialException) {
            Result.failure(Exception(context.getString(R.string.no_google_account)))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(context.getString(R.string.credential_error, e.message)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

