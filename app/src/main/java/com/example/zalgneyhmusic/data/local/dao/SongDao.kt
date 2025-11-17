package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Song Entity
 * Defines queries for database operations
 */
@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    // Synchronous version for fallback in hybrid repository
    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun getAllSongsSync(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?

    // Synchronous version for fallback
    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongByIdSync(id: String): SongEntity?

    @Query("SELECT * FROM songs ORDER BY plays DESC LIMIT :limit")
    fun getTopSongs(limit: Int = 10): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSongs(limit: Int = 10): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    // Alias for consistency
    suspend fun insert(song: SongEntity) = insertSong(song)
    suspend fun insertAll(songs: List<SongEntity>) = insertSongs(songs)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    // Alias for consistency
    suspend fun deleteAll() = deleteAllSongs()
}