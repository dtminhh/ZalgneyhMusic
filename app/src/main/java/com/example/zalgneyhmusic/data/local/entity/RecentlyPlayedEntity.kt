package com.example.zalgneyhmusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey
    val songId: String,
    val timestamp: Long = System.currentTimeMillis()
)