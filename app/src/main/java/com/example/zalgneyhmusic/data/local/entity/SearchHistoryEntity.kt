package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val id: String, // ID song/artist
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val type: String, // "SONG", "ARTIST", "ALBUM"
    val timestamp: Long = System.currentTimeMillis()
)