package com.example.zalgneyhmusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    // Insert (nếu trùng ID thì thay thế -> cập nhật timestamp mới nhất)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    // Lấy danh sách, sắp xếp mới nhất lên đầu
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getHistory(): Flow<List<SearchHistoryEntity>>

    // Xóa 1 item
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun delete(id: String)

    // Xóa tất cả
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}