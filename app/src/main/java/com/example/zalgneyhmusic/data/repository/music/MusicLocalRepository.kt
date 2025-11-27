package com.example.zalgneyhmusic.data.repository.music

import com.example.zalgneyhmusic.data.Resource
import com.example.zalgneyhmusic.data.local.MusicDatabase
import com.example.zalgneyhmusic.data.model.domain.Album
import com.example.zalgneyhmusic.data.model.domain.Artist
import com.example.zalgneyhmusic.data.model.domain.Song
import com.example.zalgneyhmusic.ui.viewmodel.fragment.SearchResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MusicRepository using Local Database
 * When needing to switch to API, just create MusicApiRepository which implements MusicRepository
 */
@Singleton
class MusicLocalRepository @Inject constructor(
    database: MusicDatabase
) : MusicRepository {

    private val songDao = database.songDao()
    private val artistDao = database.artistDao()
    private val albumDao = database.albumDao()

    // Songs
    override fun getAllSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            songDao.getAllSongs().map { entities ->
                entities.map { it.toDomain() }
            }.collect { songs ->
                emit(Resource.Success(songs))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getTopSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            songDao.getTopSongs(limit).map { entities ->
                entities.map { it.toDomain() }
            }.collect { songs ->
                emit(Resource.Success(songs))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getRecentSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            songDao.getRecentSongs(limit).map { entities ->
                entities.map { it.toDomain() }
            }.collect { songs ->
                emit(Resource.Success(songs))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getNewSongs(limit: Int): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            // Tạm thời sử dụng cùng nguồn với recentSongs (createdAt DESC)
            songDao.getRecentSongs(limit).map { entities ->
                entities.map { it.toDomain() }
            }.collect { songs ->
                emit(Resource.Success(songs))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getSongById(id: String): Resource<Song> {
        return try {
            val song = songDao.getSongById(id)?.toDomain()
            if (song != null) {
                Resource.Success(song)
            } else {
                Resource.Failure(Exception("Song not found"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading)
        try {
            songDao.searchSongs(query).map { entities ->
                entities.map { it.toDomain() }
            }.collect { songs ->
                emit(Resource.Success(songs))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // Artists
    override fun getAllArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)
        try {
            artistDao.getAllArtists().map { entities ->
                entities.map { it.toDomain() }
            }.collect { artists ->
                emit(Resource.Success(artists))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getTopArtists(limit: Int): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading)
        try {
            artistDao.getTopArtists(limit).map { entities ->
                entities.map { it.toDomain() }
            }.collect { artists ->
                emit(Resource.Success(artists))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getArtistById(id: String): Resource<Artist> {
        return try {
            val artist = artistDao.getArtistById(id)?.toDomain()
            if (artist != null) {
                Resource.Success(artist)
            } else {
                Resource.Failure(Exception("Artist not found"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun getSongsByArtist(artistId: String): Resource<List<Song>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlbumsByArtist(artistId: String): Resource<List<Album>> {
        TODO("Not yet implemented")
    }

    // Albums
    override fun getAllAlbums(): Flow<Resource<List<Album>>> = flow {
        emit(Resource.Loading)
        try {
            albumDao.getAllAlbums().map { entities ->
                entities.map { it.toDomain() }
            }.collect { albums ->
                emit(Resource.Success(albums))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override fun getRecentAlbums(limit: Int): Flow<Resource<List<Album>>> = flow {
        emit(Resource.Loading)
        try {
            albumDao.getRecentAlbums(limit).map { entities ->
                entities.map { it.toDomain() }
            }.collect { albums ->
                emit(Resource.Success(albums))
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    override suspend fun getAlbumById(id: String): Resource<Album> {
        return try {
            val album = albumDao.getAlbumById(id)?.toDomain()
            if (album != null) {
                Resource.Success(album)
            } else {
                Resource.Failure(Exception("Album not found"))
            }
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override fun searchEverything(query: String): Flow<Resource<SearchResults>> {
        TODO("Not yet implemented")
    }
}