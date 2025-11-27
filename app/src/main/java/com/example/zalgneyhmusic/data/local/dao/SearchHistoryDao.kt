package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    // Insert a history entry.
    // If an entry with the same ID already exists, replace it (refreshing its timestamp).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    // Retrieve history items ordered by newest first.
    // Limited to the most recent 20 entries.
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getHistory(): Flow<List<SearchHistoryEntity>>

    // Delete a single history entry by its ID.
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun delete(id: String)

    // Clear all search history entries.
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}