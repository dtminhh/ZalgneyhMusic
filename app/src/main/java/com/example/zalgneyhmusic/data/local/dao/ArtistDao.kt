package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Artist Entity
 */
@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    // Synchronous version for fallback
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtistsSync(): List<ArtistEntity>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: String): ArtistEntity?

    // Synchronous version
    @Query("SELECT * FROM artists WHERE id = :id")
    fun getArtistByIdSync(id: String): ArtistEntity?

    @Query("SELECT * FROM artists ORDER BY followers DESC LIMIT :limit")
    fun getTopArtists(limit: Int = 10): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    // Aliases for consistency
    suspend fun insert(artist: ArtistEntity) = insertArtist(artist)
    suspend fun insertAll(artists: List<ArtistEntity>) = insertArtists(artists)

    @Query("DELETE FROM artists WHERE id = :id")
    suspend fun deleteArtist(id: String)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    // Alias
    suspend fun deleteAll() = deleteAllArtists()
}
