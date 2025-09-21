package com.example.zalgneyhmusic.di

import com.example.zalgneyhmusic.data.model.repository.AuthRepository
import com.example.zalgneyhmusic.data.model.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}