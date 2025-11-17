package com.example.zalgneyhmusic.di

import com.example.zalgneyhmusic.data.model.api.AlbumDeserializer
import com.example.zalgneyhmusic.data.model.api.AlbumDTO
import com.example.zalgneyhmusic.data.model.api.PlaylistDTO
import com.example.zalgneyhmusic.data.model.api.PlaylistDeserializer
import com.example.zalgneyhmusic.service.ZalgneyhApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger Hilt Module for Network dependencies
 * Provides Retrofit, OkHttpClient, and API Service
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for Zalgneyh Backend API
     * Railway deployment: https://zalgneyh-backend-production.up.railway.app/api/
     */
    private const val BASE_URL = "https://zalgneyh-backend-production.up.railway.app/api/"

    /**
     * Provides Gson instance for JSON serialization/deserialization
     * - Registers AlbumDeserializer to handle flexible album.artist field
     *   (can be STRING id or OBJECT with full artist data)
     * - Registers PlaylistDeserializer to handle songs as IDs or objects
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            // Register deserializer for AlbumDTO (not domain Album)
            .registerTypeAdapter(AlbumDTO::class.java, AlbumDeserializer())
            // Register deserializer for PlaylistDTO (not domain Playlist)
            .registerTypeAdapter(PlaylistDTO::class.java, PlaylistDeserializer())
            .create()
    }

    /**
     * Provides OkHttpClient with logging interceptor
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides ZalgneyhApiService
     */
    @Provides
    @Singleton
    fun provideZalgneyhApiService(retrofit: Retrofit): ZalgneyhApiService {
        return retrofit.create(ZalgneyhApiService::class.java)
    }
}
