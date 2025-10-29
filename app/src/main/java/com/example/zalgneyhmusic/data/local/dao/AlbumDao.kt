package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Album Entity
 */
@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY releaseYear DESC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Query("SELECT * FROM albums ORDER BY releaseYear DESC LIMIT :limit")
    fun getRecentAlbums(limit: Int = 10): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbum(id: String)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()
}