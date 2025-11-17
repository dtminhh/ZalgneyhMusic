package com.example.zalgneyhmusic.data.model.utils

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Awaits the completion of a Firebase [Task] and returns its result as a suspend function.
 *
 * This extension bridges Firebase's callback-based API with Kotlin coroutines,
 * allowing you to call Firebase tasks in a sequential style using `await()`.
 *
 * Example:
 * ```
 * val user = firebaseAuth.signInWithEmailAndPassword(email, password).await()
 * ```
 *
 * Behavior:
 * - If the task completes successfully, the result is returned.
 * - If the task fails, the coroutine resumes with the exception.
 * - Supports coroutine cancellation.
 *
 * @param T The type of result produced by the [Task].
 * @return The result of the completed [Task].
 * @throws Exception If the task fails with an exception.
 */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (!cont.isActive) return@addOnCompleteListener

        val exception = task.exception
        when {
            task.isSuccessful -> {
                @Suppress("UNCHECKED_CAST")
                cont.resume(task.result as T)
            }
            exception != null -> cont.resumeWithException(exception)
            else -> cont.resumeWithException(IllegalStateException("Task ${this::class.java.simpleName} failed without exception"))
        }
    }

    cont.invokeOnCancellation {
        // Firebase Task API doesn't expose a direct cancellation hook, so we just ignore.
    }
}