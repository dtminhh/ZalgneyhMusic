package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.RecentlyPlayedEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    /**
     * Save listening history (replaces existing entry with latest timestamp if song already exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentlyPlayedEntity)

    /**
     * Get list of recently played songs, sorted by most recent first
     */
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN recently_played rp ON s.id = rp.songId
        ORDER BY rp.timestamp DESC
        LIMIT :limit
    """)
    fun getRecentlyPlayedSongs(limit: Int = 100): Flow<List<SongEntity>>

    /**
     * Clear listening history (optional operation)
     */
    @Query("DELETE FROM recently_played")
    suspend fun clearHistory()
}