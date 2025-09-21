package com.example.zalgneyhmusic.data

/**
 * A sealed class that represents the state of a resource being loaded or processed.
 *
 * This is a common pattern used in Android development to handle asynchronous data
 * (e.g., network requests, Firebase calls) in a clean and type-safe way.
 *
 * @param R The type of the data being held.
 */
sealed class Resource<out R> {
    /**
     * Represents a successful state containing the [result] data.
     *
     * @param result The data retrieved successfully.
     */
    data class Success<out R>(val result: R) : Resource<R>()

    /**
     * Represents a failure state containing an [exception].
     *
     * @param exception The exception that occurred during the operation.
     */
    data class Failure(val exception: Exception) : Resource<Nothing>()

    /**
     * Represents a loading state, typically used while an operation is in progress.
     */
    object Loading : Resource<Nothing>()
}