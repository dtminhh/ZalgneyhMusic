package com.example.zalgneyhmusic.data.model.api

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 * Matches backend response format:
 * {
 *   "success": true,
 *   "message": "Optional message",
 *   "data": { ... }
 * }
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("pagination")
    val pagination: Pagination? = null
)

/**
 * Pagination metadata
 */
data class Pagination(
    @SerializedName("currentPage")
    val currentPage: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("totalItems")
    val totalItems: Int,

    @SerializedName("itemsPerPage")
    val itemsPerPage: Int? = null
)

