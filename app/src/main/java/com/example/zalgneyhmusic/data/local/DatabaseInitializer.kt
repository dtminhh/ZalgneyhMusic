package com.example.zalgneyhmusic.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for populate sample data into database
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: MusicDatabase
) {

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // check available data
        val existingSongs = database.songDao().getAllSongs().first()
        if (existingSongs.isNotEmpty()) {
            return@withContext // Database already has data -> not init again
        }
    }
}