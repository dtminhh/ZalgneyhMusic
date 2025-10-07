package com.example.zalgneyhmusic.data.repository

import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.api.MusicApiService
import com.example.zalgneyhmusic.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val apiService: MusicApiService
) {

    // ===== SONGS =====

    fun getAllSongs(page: Int = 1, limit: Int = 20): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getAllSongs(page, limit)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load songs")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getSongById(id: String): Flow<Resource<Song>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getSongById(id)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load song")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.searchSongs(query)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Search failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getTrendingSongs(limit: Int = 10): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getTrendingSongs(limit)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load trending songs")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getNewSongs(limit: Int = 10): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getNewSongs(limit)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load new songs")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getSongsByGenre(genre: String, limit: Int = 20): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getSongsByGenre(genre, limit)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load songs by genre")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // ===== ARTISTS =====

    fun getAllArtists(page: Int = 1, limit: Int = 20): Flow<Resource<List<Artist>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getAllArtists(page, limit)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load artists")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getArtistById(id: String): Flow<Resource<Artist>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getArtistById(id)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load artist")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getArtistSongs(artistId: String): Flow<Resource<List<Song>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getArtistSongs(artistId)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load artist songs")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // ===== PLAYLISTS =====

    fun getAllPlaylists(): Flow<Resource<List<Playlist>>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getAllPlaylists()
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load playlists")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getPlaylistById(id: String): Flow<Resource<Playlist>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.getPlaylistById(id)
            if (response.success) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Failure(Exception("Failed to load playlist")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }
}
