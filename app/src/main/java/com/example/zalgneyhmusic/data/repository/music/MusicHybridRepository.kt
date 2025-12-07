package com.example.zalgneyhmusic.data.repository.music

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.zalgneyhmusic.data.model.domain.DownloadState
import com.example.zalgneyhmusic.data.model.Resource
import com.example.zalgneyhmusic.data.local.MusicDatabase
import com.example.zalgneyhmusic.data.local.dao.AlbumDao
import com.example.zalgneyhmusic.data.local.dao.ArtistDao
import com.example.zalgneyhmusic.data.local.dao.SongDao
import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import com.example.zalgneyhmusic.data.local.entity.RecentlyPlayedEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Playlist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.utils.await
import com.example.zalgneyhmusic.data.session.UserManager
import com.example.zalgneyhmusic.service.ZalgneyhApiService
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SearchResults
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
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
    private val albumDao: AlbumDao,
    private val firebaseAuth: FirebaseAuth,
    private val userManager: UserManager,
    database: MusicDatabase,
    @ApplicationContext private val context: Context
) : MusicRepository {

    private val recentlyPlayedDao = database.recentlyPlayedDao()

    companion object {
        private const val TAG = "MusicHybridRepository"
    }

    // ==================== SONGS ====================

    override fun downloadSong(song: Song): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0))

        try {
            // Setup file path using app-specific storage (no permission required)
            val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(musicDir, "${song.id}.mp3")
            val entity = songDao.getSongById(song.id)

            // If file already exists, update DB path and return success
            if (file.exists() && entity != null && entity.localPath == null) {
                val updatedEntity = entity.copy(localPath = file.absolutePath)
                songDao.insert(updatedEntity)
                emit(DownloadState.Success)
                return@flow
            }

            if (entity == null) {
                emit(DownloadState.Failure(Exception("Song not found in cache for download")))
                return@flow
            }

            // Download file from URL
            val url = URL(song.url)
            val connection = url.openConnection()
            connection.connect()

            val fileSize = connection.contentLength
            val inputStream = BufferedInputStream(url.openStream())
            val outputStream = FileOutputStream(file)

            val data = ByteArray(8192) // 8KB buffer size
            var totalBytesRead: Long = 0
            var bytesRead: Int

            while (inputStream.read(data).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                outputStream.write(data, 0, bytesRead)

                // Calculate and emit download progress
                if (fileSize > 0) {
                    val progress = ((totalBytesRead * 100) / fileSize).toInt().coerceIn(0, 100)
                    emit(DownloadState.Downloading(progress))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            val updatedEntity = entity.copy(localPath = file.absolutePath)
            songDao.insert(updatedEntity)

            emit(DownloadState.Success)

        } catch (e: Exception) {
            emit(DownloadState.Failure(e))
            // Clean up incomplete file on error
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "${song.id}.mp3"
            ).delete()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun removeDownloadedSong(songId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val entity = songDao.getSongById(songId)

                if (entity?.localPath != null) {
                    // Delete physical file
                    val file = File(entity.localPath)
                    if (file.exists()) {
                        file.delete()
                    }

                    // Update DB (set localPath = null)
                    val updatedEntity = entity.copy(localPath = null)
                    songDao.insert(updatedEntity)

                    Resource.Success(Unit)
                } else {
                    Resource.Failure(Exception("Song has not been downloaded"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    override fun getDownloadedSongs(): Flow<List<Song>> {
        return songDao.getDownloadedSongs()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getSongFlow(songId: String): Flow<Song?> {
        return songDao.getSongFlow(songId).map { it?.toDomain() }
    }

    /**
     * Fetch all songs from API (page 1, limit 100), cache them in local DB.
     * If API fails, fallback to cached songs.
     */
    override fun getAllSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Try API first
            val response = apiService.getAllSongs(page = 1, limit = 100)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }

                val entitiesToInsert = mergeWithLocalCache(songs)

                // Update cache
                songDao.insertAll(entitiesToInsert)

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
                emit(Resource.Failure(Exception("Failed to fetch data from API and no cached data available")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch top trending songs from API, cache them.
     * Fallback: sort cached songs by plays descending.
     *
     * @param limit Number of top songs to fetch
     */
    override fun getTopSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Use dedicated trending endpoint; backend already sorts by popularity
            val response = apiService.getTrendingSongs(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }

                // Update cache
                val entitiesToInsert = mergeWithLocalCache(songs)
                songDao.insertAll(entitiesToInsert)

                Log.d(TAG, "getTopSongs: Fetched ${songs.size} trending songs")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopSongs: API error", e)
        }

        // Fallback to cache (approximate top by plays)
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
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch recent songs using server-side sorting by creation date.
     * Fallback: sort cached songs by createdAt.
     *
     * @param limit Max number of recent songs
     */
    override fun getRecentSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            // Backend API does not expose sort params here; fetch limited set then sort client-side.
            // TODO: switch to a dedicated recent endpoint or server-side sorting when available.
            val response = apiService.getAllSongs(
                limit = limit
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }
                    .sortedByDescending { it.createdAt }

                val entitiesToInsert = mergeWithLocalCache(songs)
                songDao.insertAll(entitiesToInsert)

                Log.d(TAG, "getRecentSongs: Fetched ${songs.size} songs")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecentSongs: API error", e)
        }

        // Fallback: use cached songs
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
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch new songs from API (backend-defined new), cache them.
     * Fallback: sort cached songs by createdAt descending.
     *
     * @param limit Number of new songs
     */
    override fun getNewSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)

        try {
            val response = apiService.getNewSongs(limit = limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val songDTOs = response.body()?.data ?: emptyList()
                val songs = songDTOs.map { it.toDomain() }

                val entitiesToInsert = mergeWithLocalCache(songs)
                songDao.insertAll(entitiesToInsert)

                Log.d(TAG, "getNewSongs: Fetched ${songs.size} new songs")
                emit(Resource.Success(songs))
                return@flow
            }
        } catch (e: Exception) {
            Log.e(TAG, "getNewSongs: API error", e)
        }

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
    }.flowOn(Dispatchers.IO)

    private fun mergeWithLocalCache(apiSongs: List<Song>): List<SongEntity> {
        // Get all existing songs from DB (to retrieve old localPath values)
        val localSongs = songDao.getAllSongsSync()

        // Create map: ID -> LocalPath
        val localPathMap = localSongs.associate { it.id to it.localPath }

        // Map API data to entities, preserving existing localPath if available
        return apiSongs.map { song ->
            val entity = SongEntity.fromDomain(song)
            val existingPath = localPathMap[song.id]

            // If old DB already has a path, preserve it in the new entity
            if (existingPath != null) {
                entity.copy(localPath = existingPath)
            } else {
                entity
            }
        }
    }

    /**
     * Get a single song by ID. Try API first, fallback to cached song if API fails.
     *
     * @param id Song ID
     * @return Resource<Song>
     */
    override suspend fun getSongById(id: String): Resource<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSongById(id)
            if (response.isSuccessful && response.body()?.success == true) {
                val song = response.body()?.data?.toDomain()
                if (song != null) {
                    // Retrieve old localPath safely on IO Thread
                    val oldEntity = songDao.getSongByIdSync(id)
                    val newEntity = SongEntity.fromDomain(song).let {
                        if (oldEntity?.localPath != null) {
                            it.copy(localPath = oldEntity.localPath)
                        } else {
                            it
                        }
                    }

                    songDao.insert(newEntity)
                    Resource.Success(newEntity.toDomain())
                } else {
                    Resource.Failure(Exception("Song not found"))
                }
            } else {
                // Fallback to cache safely on IO Thread
                val cached = songDao.getSongByIdSync(id)?.toDomain()
                cached?.let { Resource.Success(it) } ?: Resource.Failure(Exception("Song not found"))
            }
        } catch (e: Exception) {
            val cached = songDao.getSongByIdSync(id)?.toDomain()
            cached?.let { Resource.Success(it) } ?: Resource.Failure(e)
        }
    }

    /**
     * Search songs locally by title or artist name.
     *
     * @param query Search string
     */
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

    override suspend fun addToRecentlyPlayed(song: Song) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Ensure SongEntity exists in songs table first (to prevent JOIN errors)
                songDao.insert(SongEntity.fromDomain(song))

                // 2. Save to listening history table
                recentlyPlayedDao.insert(RecentlyPlayedEntity(songId = song.id))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getListeningHistory(): Flow<List<Song>> {
        return recentlyPlayedDao.getRecentlyPlayedSongs()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getPersonalizedSuggestions(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            // 1. Gather seed data
            // Get the 50 most recently played songs to analyze current preferences
            val historyEntities = recentlyPlayedDao.getRecentlyPlayedSongs(limit = 50).first()
            val historySongs = historyEntities.map { it.toDomain() }

            // Get the list of favorite song IDs
            val favoriteIds = userManager.favoriteSongIds.value

            // 2. Build user profile (preference profile)
            // Count plays per artist
            val artistAffinity = historySongs.groupingBy { it.artist.id }.eachCount()
            // Count plays per genre (if genre exists)
            val genreAffinity = historySongs.groupingBy { it.genre ?: "Unknown" }.eachCount()

            // 3. Candidate generation
            // Load all songs currently in cache (offline)
            val allSongs = songDao.getAllSongsSync().map { it.toDomain() }

            // 4. Scoring
            val scoredSongs = allSongs.map { song ->
                var score = 0.0

                // A. Artist score (most important)
                // Example: if artist listened 5 times -> songs by that artist get +15 points
                val artistCount = artistAffinity[song.artist.id] ?: 0
                score += artistCount * 3.0

                // B. Genre score (second important)
                val genreCount = genreAffinity[song.genre ?: "Unknown"] ?: 0
                score += genreCount * 1.0

                // C. Favorite score
                if (favoriteIds.contains(song.id)) {
                    score += 5.0
                }

                // D. Random score (discovery)
                // Add small randomness to avoid monotony
                score += Math.random() * 2.0

                // E. Heavy penalty for recently played songs (avoid repeating recent plays)
                if (historySongs.any { it.id == song.id }) {
                    score -= 100.0
                }

                song to score
            }

            // 5. Ranking & filtering
            val suggestions = scoredSongs
                .filter { it.second > 0 } // Keep only songs with positive score
                .sortedByDescending { it.second } // Higher score first
                .map { it.first }
                .take(20) // Take top 20

            // If algorithm finds no candidates (new user), return random or top songs
            if (suggestions.isEmpty()) {
                // Fallback: return random 10 songs
                emit(Resource.Success(allSongs.shuffled().take(10)))
            } else {
                emit(Resource.Success(suggestions))
            }

        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Toggle favorite status of a song in a playlist.
     *
     * @param playlistId Playlist ID (usually favorites playlist)
     * @param songId Song ID to toggle
     * @return Resource<Boolean> - true if added, false if removed
     */
    override suspend fun toggleFavorite(playlistId: String, songId: String): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                // Get authenticated user and token
                val user = firebaseAuth.currentUser
                    ?: return@withContext Resource.Failure(Exception("Please login"))
                val token = "Bearer ${user.getIdToken(false).await().token}"

                // Call API to toggle song in playlist
                val response = apiService.toggleSongInPlaylist(
                    token,
                    playlistId,
                    mapOf("songId" to songId)
                )

                // Process response
                if (response.isSuccessful && response.body()?.success == true) {
                    // Parse isAdded result from server
                    val dataMap = response.body()!!.data as? Map<*, *>
                    val isAdded = dataMap?.get("isAdded") as? Boolean ?: false

                    // Update UserManager immediately (optimistic update)
                    if (isAdded) {
                        userManager.addFavoriteSong(songId)
                    } else {
                        userManager.removeFavoriteSong(songId)
                    }
                    Resource.Success(isAdded)
                } else {
                    Resource.Failure(Exception("Server connection error"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    /**
     * Hybrid search: fetch Songs, Artists, and Albums from API based on query.
     *
     * @param query Search string
     * @return SearchResults containing songs, artists, albums
     */
    override fun searchEverything(query: String): Flow<Resource<SearchResults>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.search(query)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data

                val results = SearchResults(
                    songs = data?.songs?.map { it.toDomain() } ?: emptyList(),
                    artists = data?.artists?.map { it.toDomain() } ?: emptyList(),
                    albums = data?.albums?.map { it.toDomain() } ?: emptyList()
                )
                emit(Resource.Success(results))
            } else {
                emit(Resource.Failure(Exception("Search failed")))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // ==================== ARTISTS ====================

    /**
     * Fetch all artists from API and cache them.
     * Fallback: use cached artists.
     */
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

    /**
     * Fetch top artists sorted by followers.
     * Fallback: use cached artists sorted by followers.
     *
     * @param limit Number of top artists
     */
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
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch artist by ID, try API first, fallback to cache.
     *
     * @param id Artist ID
     */
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

    /**
     * Fetch songs of a specific artist from API.
     *
     * @param artistId Artist ID
     */
    override suspend fun getSongsByArtist(artistId: String): Resource<List<Song>> {
        return try {
            val response = apiService.getArtistSongs(artistId)
            if (response.isSuccessful && response.body()?.success == true) {
                val songs = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                Resource.Success(songs)
            } else {
                Resource.Failure(Exception("Failed to load artist songs"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Fetch albums of a specific artist from API.
     *
     * @param artistId Artist ID
     */
    override suspend fun getAlbumsByArtist(artistId: String): Resource<List<Album>> {
        return try {
            val response = apiService.getAlbumsByArtist(artistId)
            if (response.isSuccessful && response.body()?.success == true) {
                val albums = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                Resource.Success(albums)
            } else {
                Resource.Failure(Exception("Failed to load artist albums"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Get list of artists that the current user is following.
     *
     * @return Resource<List<Artist>> - List of followed artists
     */
    override suspend fun getFollowedArtists(): Resource<List<Artist>> =
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                    ?: return@withContext Resource.Failure(Exception("Login required"))
                val token = "Bearer ${user.getIdToken(false).await().token}"

                // Call API (Backend has route /api/users/artists)
                val response = apiService.getFollowedArtists(token)

                if (response.isSuccessful && response.body()?.success == true) {
                    val artists = response.body()!!.data!!.map { it.toDomain() }

                    // Save artist IDs to UserManager
                    userManager.setFollowedArtistIds(artists.map { it.id })

                    Resource.Success(artists)
                } else {
                    Resource.Failure(Exception("Failed to load artists"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    /**
     * Toggle follow status for an artist.
     *
     * @param artistId Artist ID to follow/unfollow
     * @return Resource<Boolean> - true if following, false if unfollowed
     */
    override suspend fun toggleFollowArtist(artistId: String): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                    ?: return@withContext Resource.Failure(Exception("Login required"))
                val token = "Bearer ${user.getIdToken(false).await().token}"

                val response = apiService.toggleFollow(token, mapOf("artistId" to artistId))

                if (response.isSuccessful && response.body()?.success == true) {
                    // Parse result from backend { isFollowing: true/false }
                    val data = response.body()!!.data as? Map<*, *>
                    val isFollowing = data?.get("isFollowing") as? Boolean ?: false

                    // Update UserManager to ensure sync
                    if (isFollowing) userManager.followArtist(artistId)
                    else userManager.unfollowArtist(artistId)

                    Resource.Success(isFollowing)
                } else {
                    Resource.Failure(Exception("Failed"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    // ==================== ALBUMS ====================

    /**
     * Fetch all albums from API and cache them.
     * Fallback: use cached albums.
     */
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

    /**
     * Fetch recent albums sorted by releaseYear.
     * Fallback: use cached albums sorted by releaseYear.
     *
     * @param limit Number of recent albums
     */
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

    /**
     * Fetch album by ID, try API first, fallback to cache.
     *
     * @param id Album ID
     */
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

    /**
     * Create a new playlist with optional image.
     *
     * @param name Playlist name
     * @param description Playlist description (optional)
     * @param imageFile Image file for playlist cover (optional)
     * @return Resource<Playlist> - Created playlist
     */
    override suspend fun createPlaylist(
        name: String,
        description: String?,
        imageFile: File?
    ): Resource<Playlist> =
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                    ?: return@withContext Resource.Failure(Exception("Login required"))
                val token = "Bearer ${user.getIdToken(false).await().token}"

                // Create data parts
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    okhttp3.MultipartBody.Part.createFormData("image", it.name, requestFile)
                }

                // Call API
                val response = apiService.createPlaylist(token, namePart, descPart, imagePart)

                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(response.body()!!.data!!.toDomain())
                } else {
                    Resource.Failure(Exception("Create playlist failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    /**
     * Get all playlists of the current user.
     * Also updates favorite song IDs if favorite playlist exists.
     *
     * @return Resource<List<Playlist>> - User's playlists
     */
    override suspend fun getMyPlaylists(): Resource<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return@withContext Resource.Failure(Exception("Login required"))
            }
            // Get token (forceRefresh = false to use cache for speed)
            val tokenResult = user.getIdToken(false).await()
            val token = "Bearer ${tokenResult.token}"

            val response = apiService.getMyPlaylists(token)
            if (response.isSuccessful && response.body()?.success == true) {
                val playlists = response.body()!!.data!!.map { it.toDomain() }

                // Use local variable name to avoid confusion with Firebase 'user' variable above
                val currentAppUser = userManager.currentUserValue
                if (currentAppUser?.favoritePlaylistId != null) {
                    val favPlaylist = playlists.find { it.id == currentAppUser.favoritePlaylistId }
                    if (favPlaylist != null) {
                        // Extract song IDs from favorite playlist
                        // songs is now List<Song>, so we need to map .id
                        val ids = favPlaylist.songs.map { it.id }
                        // Save song IDs to session
                        userManager.setFavoriteSongIds(ids)
                    }
                }
                Resource.Success(playlists)
            } else {
                Resource.Failure(Exception("Get playlists failed"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Add a song to a playlist.
     *
     * @param playlistId Playlist ID
     * @param songId Song ID to add
     * @return Resource<Any> - API response data
     */
    override suspend fun addSongToPlaylist(
        playlistId: String,
        songId: String
    ): Resource<Any> = withContext(Dispatchers.IO) {
        try {
            val user = firebaseAuth.currentUser
                ?: return@withContext Resource.Failure(Exception("Login required"))
            val tokenResult = user.getIdToken(false).await()
            val token = "Bearer ${tokenResult.token}"
            val response =
                apiService.addSongToPlaylist(token, playlistId, mapOf("songId" to songId))
            if (response.isSuccessful && response.body()?.success == true) {
                val playlists = response.body()!!.data!!
                Resource.Success(playlists)
            } else {
                Resource.Failure(Exception("Add song to playlist failed"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    /**
     * Delete a playlist by ID.
     *
     * @param id Playlist ID to delete
     * @return Resource<Boolean> - true if successful
     */
    override suspend fun deletePlaylist(id: String): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                    ?: return@withContext Resource.Failure(Exception("Login required"))
                val token = "Bearer ${user.getIdToken(false).await().token}"
                val response = apiService.deletePlaylist(token, id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(true)
                } else {
                    Resource.Failure(Exception("Delete failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Resource.Failure(e)
            }
        }

    /**
     * Update a playlist's name and/or image.
     *
     * @param id Playlist ID to update
     * @param name New playlist name
     * @param imageFile New image file (optional)
     * @return Resource<Playlist> - Updated playlist
     */
    override suspend fun updatePlaylist(
        id: String,
        name: String,
        imageFile: File?
    ): Resource<Playlist> = withContext(Dispatchers.IO) {
        try {
            val user = firebaseAuth.currentUser
                ?: return@withContext Resource.Failure(Exception("Login required"))
            val token = "Bearer ${user.getIdToken(false).await().token}"

            val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
            // Create image part if provided
            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                okhttp3.MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response = apiService.updatePlaylist(
                token,
                id,
                namePart,
                null,
                imagePart
            ) // Description temporarily null
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Failure(Exception("Update failed"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }
}
