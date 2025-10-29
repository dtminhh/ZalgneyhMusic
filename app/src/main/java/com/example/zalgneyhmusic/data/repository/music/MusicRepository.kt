package com.example.zalgneyhmusic.data.repository.music

import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.data.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining methods for Music Repository
 * Enables easy switching between Local Database and API
 */
interface MusicRepository {
    // Songs
    fun getAllSongs(): Flow<Resource<List<Song>>>
    fun getTopSongs(limit: Int = 10): Flow<Resource<List<Song>>>
    fun getRecentSongs(limit: Int = 10): Flow<Resource<List<Song>>>
    suspend fun getSongById(id: String): Resource<Song>
    fun searchSongs(query: String): Flow<Resource<List<Song>>>

    // Artists
    fun getAllArtists(): Flow<Resource<List<Artist>>>
    fun getTopArtists(limit: Int = 10): Flow<Resource<List<Artist>>>
    suspend fun getArtistById(id: String): Resource<Artist>

    // Albums
    fun getAllAlbums(): Flow<Resource<List<Album>>>
    fun getRecentAlbums(limit: Int = 10): Flow<Resource<List<Album>>>
    suspend fun getAlbumById(id: String): Resource<Album>
}

