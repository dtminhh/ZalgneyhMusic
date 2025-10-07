package com.example.zalgneyhmusic.di

import com.example.zalgneyhmusic.data.model.repository.AuthRepository
import com.example.zalgneyhmusic.data.model.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.zalgneyhmusic.data.model.utils.GoogleSignInHelper
import com.example.zalgneyhmusic.data.api.MusicApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Hilt module that provides application-level dependencies.
 *
 * This module is installed in the [SingletonComponent], meaning
 * all the provided dependencies will live as long as the application.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    companion object {
        private const val BASE_URL = "http://192.168.5.4:3000/"
    }

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

    // ===== API PROVIDERS =====

    /**
     * Provides logging interceptor for debugging API calls
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides OkHttpClient with timeout and logging configuration
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance configured with base URL and Gson converter
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides MusicApiService for making API calls
     */
    @Provides
    @Singleton
    fun provideMusicApiService(retrofit: Retrofit): MusicApiService {
        return retrofit.create(MusicApiService::class.java)
    }
}