package com.example.zalgneyhmusic.data.repository.music

import android.util.Log
import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.local.dao.AlbumDao
import com.example.zalgneyhmusic.data.local.dao.ArtistDao
import com.example.zalgneyhmusic.data.local.dao.SongDao
import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.service.ZalgneyhApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Hybrid Repository Implementation
 * Strategy: Try API first, fallback to local cache on failure
 *
 * Benefits:
 * - Always shows fresh data when online
 * - Works offline with cached data
 * - Automatic cache updates
 */
class MusicHybridRepository @Inject constructor(
    private val apiService: ZalgneyhApiService,
    private val songDao: SongDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao
) : MusicRepository {

    companion object {
        private const val TAG = "MusicHybridRepository"
    }

    // ==================== SONGS ====================

    override fun getAllSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Try API first
            val response = apiService.getAllSongs(page = 1, limit = 100)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }

                // Update cache
                songDao.deleteAll()
                songDao.insertAll(songs.map { SongEntity.fromDomain(it) })

                Log.d(TAG, "getAllSongs: Fetched ${songs.size} songs from API")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllSongs: API error", e)
        }

        // Fallback to cache
        try {
            val cachedSongs = songDao.getAllSongsSync().map { it.toDomain() }
            if (cachedSongs.isNotEmpty()) {
                Log.d(TAG, "getAllSongs: Using ${cachedSongs.size} cached songs")
                emit(Resource.Success(cachedSongs))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getTopSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Use getAllSongs and take top N by plays
            val response = apiService.getAllSongs(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }
                    .sortedByDescending { it.plays }
                    .take(limit)

                // Update cache
                songDao.insertAll(songs.map { SongEntity.fromDomain(it) })

                Log.d(TAG, "getTopSongs: Fetched ${songs.size} songs")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopSongs: API error", e)
        }

        // Fallback to cache
        try {
            val cached = songDao.getAllSongsSync()
                .map { it.toDomain() }
                .sortedByDescending { it.plays }
                .take(limit)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getRecentSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Use getAllSongs and take most recent
            val response = apiService.getAllSongs(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }
                    .sortedByDescending { it.createdAt }
                    .take(limit)

                songDao.insertAll(songs.map { SongEntity.fromDomain(it) })

                Log.d(TAG, "getRecentSongs: Fetched ${songs.size} songs")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecentSongs: API error", e)
        }

        // Fallback
        try {
            val cached = songDao.getAllSongsSync()
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
                .take(limit)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getSongById(id: String): Resource<Song> {
        return try {
            val response = apiService.getSongById(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val song = response.body()?.data?.toDomain()
                if (song != null) {
                    // Update cache
                    songDao.insert(SongEntity.fromDomain(song))
                    Resource.Success(song)
                } else {
                    Resource.Failure(Exception("Song not found"))
                }
            } else {
                // Fallback to cache
                val cached = songDao.getSongByIdSync(id)?.toDomain()
                cached?.let { Resource.Success(it) }
                    ?: Resource.Failure(Exception("Song not found"))
            }
        } catch (e: Exception) {
            // Fallback to cache
            val cached = songDao.getSongByIdSync(id)?.toDomain()
            cached?.let { Resource.Success(it) }
                ?: Resource.Failure(e)
        }
    }

    override fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        // Search in cache (API doesn't have search endpoint yet)
        try {
            val results = songDao.getAllSongsSync()
                .map { it.toDomain() }
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.artist.name.contains(query, ignoreCase = true)
                }

            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // ==================== ARTISTS ====================

    override fun getAllArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)

        try {
            val response = apiService.getArtists(page = 1, limit = 100)
            if (response.isSuccessful && response.body()?.success == true) {
                val artistDTOs = response.body()?.data ?: emptyList()
                val artists = artistDTOs.map { it.toDomain() }

                // Update cache
                artistDao.deleteAll()
                artistDao.insertAll(artists.map { ArtistEntity.fromDomain(it) })

                Log.d(TAG, "getAllArtists: Fetched ${artists.size} artists")
                emit(Resource.Success(artists))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllArtists: API error", e)
        }

        // Fallback
        try {
            val cached = artistDao.getAllArtistsSync().map { it.toDomain() }
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getTopArtists(limit: Int): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getArtists(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val artists = (response.body()?.data ?: emptyList())
                    .map { it.toDomain() }
                    .sortedByDescending { it.followers }
                    .take(limit)

                artistDao.insertAll(artists.map { ArtistEntity.fromDomain(it) })

                emit(Resource.Success(artists))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopArtists: API error", e)
        }

        // Fallback
        try {
            val cached = artistDao.getAllArtistsSync()
                .map { it.toDomain() }
                .sortedByDescending { it.followers }
                .take(limit)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getArtistById(id: String): Resource<Artist> {
        return try {
            val response = apiService.getArtistById(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val artist = response.body()?.data?.toDomain()
                if (artist != null) {
                    artistDao.insert(ArtistEntity.fromDomain(artist))
                    Resource.Success(artist)
                } else {
                    Resource.Failure(Exception("Artist not found"))
                }
            } else {
                val cached = artistDao.getArtistByIdSync(id)?.toDomain()
                cached?.let { Resource.Success(it) }
                    ?: Resource.Failure(Exception("Artist not found"))
            }
        } catch (e: Exception) {
            val cached = artistDao.getArtistByIdSync(id)?.toDomain()
            cached?.let { Resource.Success(it) }
                ?: Resource.Failure(e)
        }
    }

    // ==================== ALBUMS ====================

    override fun getAllAlbums(): Flow<Resource<List<Album>>> = flow {
        emit(Resource.Loading)

        try {
            val response = apiService.getAlbums(page = 1, limit = 100)
            if (response.isSuccessful && response.body()?.success == true) {
                val albumDTOs = response.body()?.data ?: emptyList()
                val albums = albumDTOs.map { it.toDomain() }

                // Update cache
                albumDao.deleteAll()
                albumDao.insertAll(albums.map { AlbumEntity.fromDomain(it) })

                Log.d(TAG, "getAllAlbums: Fetched ${albums.size} albums")
                emit(Resource.Success(albums))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllAlbums: API error", e)
        }

        // Fallback
        try {
            val cached = albumDao.getAllAlbumsSync().map { it.toDomain() }
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getRecentAlbums(limit: Int): Flow<Resource<List<Album>>> = flow {
        emit(Resource.Loading)

        try {
            val response = apiService.getAlbums(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val albums = (response.body()?.data ?: emptyList())
                    .map { it.toDomain() }
                    .sortedByDescending { it.releaseYear }
                    .take(limit)

                albumDao.insertAll(albums.map { AlbumEntity.fromDomain(it) })

                emit(Resource.Success(albums))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecentAlbums: API error", e)
        }

        // Fallback
        try {
            val cached = albumDao.getAllAlbumsSync()
                .map { it.toDomain() }
                .sortedByDescending { it.releaseYear }
                .take(limit)
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Failure(Exception("No data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getAlbumById(id: String): Resource<Album> {
        return try {
            val response = apiService.getAlbumById(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val album = response.body()?.data?.toDomain()
                if (album != null) {
                    albumDao.insert(AlbumEntity.fromDomain(album))
                    Resource.Success(album)
                } else {
                    Resource.Failure(Exception("Album not found"))
                }
            } else {
                val cached = albumDao.getAlbumByIdSync(id)?.toDomain()
                cached?.let { Resource.Success(it) }
                    ?: Resource.Failure(Exception("Album not found"))
            }
        } catch (e: Exception) {
            val cached = albumDao.getAlbumByIdSync(id)?.toDomain()
            cached?.let { Resource.Success(it) }
                ?: Resource.Failure(e)
        }
    }
}

