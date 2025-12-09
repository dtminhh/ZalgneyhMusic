package com.example.zalgneyhmusic.di

import android.content.Context
import androidx.room.Room
import com.example.zalgneyhmusic.data.local.MusicDatabase
import com.example.zalgneyhmusic.utils.GoogleSignInHelper
import com.example.zalgneyhmusic.data.repository.auth.AuthRepository
import com.example.zalgneyhmusic.data.repository.auth.AuthRepositoryImpl
import com.example.zalgneyhmusic.data.repository.music.MusicHybridRepository
import com.example.zalgneyhmusic.data.repository.music.MusicRepository
import com.example.zalgneyhmusic.data.session.UserManager
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
 * Installed in SingletonComponent: instances live for the app lifecycle.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    /** Provides FirebaseAuth singleton. */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /** Binds AuthRepository to its implementation. */
    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    /** Provides GoogleSignInHelper singleton. */
    @Provides
    @Singleton
    fun provideGoogleSignInHelper(): GoogleSignInHelper = GoogleSignInHelper()

    /** Provides Room database instance. */
    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        )
            // Allow destructive migration for development; replace with proper migrations later.
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Provides MusicRepository implementation (API + Local cache). */
    @Provides
    @Singleton
    fun provideMusicRepository(
        database: MusicDatabase,
        apiService: com.example.zalgneyhmusic.service.ZalgneyhApiService,
        @ApplicationContext context: Context
    ): MusicRepository {
        return MusicHybridRepository(
            apiService = apiService,
            songDao = database.songDao(),
            artistDao = database.artistDao(),
            albumDao = database.albumDao(),
            firebaseAuth = provideFirebaseAuth(),
            userManager = UserManager(context),
            database = database,
            context = context
        )
    }

    /** Provides MusicPlayer singleton. */
    @Provides
    @Singleton
    fun provideMusicPlayer(
        @ApplicationContext context: Context,
        exoPlayer: androidx.media3.exoplayer.ExoPlayer
    ): MusicPlayer {
        return MusicPlayer(context, exoPlayer)
    }
}