package com.example.zalgneyhmusic.data.api

import java.util.Locale

object ApiHelper {

    private const val BASE_URL = "http://192.168.5.4:3000/"

    /**
     * get URL for streaming songs
     */
    fun getStreamUrl(songId: String): String {
        return "${BASE_URL}api/songs/stream/$songId"
    }

    /**
     * get URL for song thumbnail
     */
    fun getImageUrl(imagePath: String?): String? {
        return if (imagePath != null && !imagePath.startsWith("http")) {
            "$BASE_URL${imagePath.removePrefix("/")}"
        } else {
            imagePath
        }
    }

    /**
     * Format duration: seconds - MM:SS
     */
    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US,"%d:%02d", minutes, secs)
    }
}