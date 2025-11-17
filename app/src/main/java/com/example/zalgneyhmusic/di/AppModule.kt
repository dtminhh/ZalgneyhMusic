package com.example.zalgneyhmusic.di

import android.content.Context
import androidx.room.Room
import com.example.zalgneyhmusic.data.local.MusicDatabase
import com.example.zalgneyhmusic.data.model.utils.GoogleSignInHelper
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.repository.auth.AuthRepositoryImpl
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.player.MusicPlayer
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides application-level dependencies.
 *
 * This module is installed in the [SingletonComponent], meaning
 * all the provided dependencies will live as long as the application.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    /**
     * Provides a singleton instance of [FirebaseAuth].
     *
     * @return The [FirebaseAuth] instance from Firebase.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Provides a singleton implementation of [AuthRepository].
     *
     * @param impl The [AuthRepositoryImpl] implementation injected by Hilt.
     * @return An [AuthRepository] that delegates to [AuthRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    /**
     * Provides a singleton instance of [GoogleSignInHelper] for dependency injection.
     *
     * This method is annotated with `@Provides` and `@Singleton`, meaning:
     * - Hilt/Dagger will use this function to create and supply a single instance
     *   of [GoogleSignInHelper] throughout the application lifecycle.
     * - Any class that requires [GoogleSignInHelper] in its constructor
     *   can have it automatically injected by Hilt.
     *
     * @return a singleton instance of [GoogleSignInHelper]
     */
    @Provides
    @Singleton
    fun provideGoogleSignInHelper(): GoogleSignInHelper = GoogleSignInHelper()

    /**
     * Provides Room Database instance
     */
    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        )
            .fallbackToDestructiveMigration() // Allow DB recreation on schema changes (dev mode)
            .build()
    }

    /**
     * Provides MusicRepository implementation (Hybrid: API + Local Cache)
     * Strategy: Try API first, fallback to cache on error
     */
    @Provides
    @Singleton
    fun provideMusicRepository(
        database: MusicDatabase,
        apiService: com.example.zalgneyhmusic.service.ZalgneyhApiService
    ): MusicRepository {
        return com.example.zalgneyhmusic.data.repository.music.MusicHybridRepository(
            apiService = apiService,
            songDao = database.songDao(),
            artistDao = database.artistDao(),
            albumDao = database.albumDao()
        )
    }

    /**
     * Provides MusicPlayer singleton instance
     */
    @Provides
    @Singleton
    fun provideMusicPlayer(@ApplicationContext context: Context): MusicPlayer {
        return MusicPlayer(context)
    }
}