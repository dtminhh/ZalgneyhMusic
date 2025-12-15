package com.example.zalgneyhmusic.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/** Hilt Module - Provides ExoPlayer instance for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    // Create Cache Instance (Singleton)
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayerCache(@ApplicationContext context: Context): SimpleCache {
        val cacheContentDirectory = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // cache limit 100MB
        val databaseProvider = StandaloneDatabaseProvider(context)

        return SimpleCache(cacheContentDirectory, evictor, databaseProvider)
    }

    // DataSourceFactory support Cache data
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(
        simpleCache: SimpleCache
    ): DataSource.Factory {
        // Default Source
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        // local cache + remote cache
        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    // Provide Exo Player with DataSourceFactory
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        cacheDataSourceFactory: DataSource.Factory
    ): ExoPlayer {

        // MediaSourceFactory use cache
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
