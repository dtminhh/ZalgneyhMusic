package com.example.zalgneyhmusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zalgneyhmusic.data.local.dao.AlbumDao
import com.example.zalgneyhmusic.data.local.dao.ArtistDao
import com.example.zalgneyhmusic.data.local.dao.SearchHistoryDao
import com.example.zalgneyhmusic.data.local.dao.SongDao
import com.example.zalgneyhmusic.data.local.entity.AlbumEntity
import com.example.zalgneyhmusic.data.local.entity.ArtistEntity
import com.example.zalgneyhmusic.data.local.entity.SearchHistoryEntity
import com.example.zalgneyhmusic.data.local.entity.SongEntity

/**
 * Room Database for Music application
 * Manages Entities: Song, Artist, Album
 */
@Database(
    entities = [
        SongEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        SearchHistoryEntity::class
    ],
    version = 12,  // Increased for SongEntity column type changes and AlbumEntity schema changes (artist split into artistId & artistName, added coverImage)
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}