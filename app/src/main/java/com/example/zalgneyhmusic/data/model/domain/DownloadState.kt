package com.example.zalgneyhmusic.data.model.domain

/**
 * Represents the state of a download operation.
 * Used to track download progress and status.
 */
sealed class DownloadState {

    /** Download in progress with current percentage */
    data class Downloading(val progress: Int) : DownloadState()

    /** Download completed successfully */
    data object Success : DownloadState()

    /** Download failed with an exception */
    data class Failure(val exception: Throwable) : DownloadState()

}

