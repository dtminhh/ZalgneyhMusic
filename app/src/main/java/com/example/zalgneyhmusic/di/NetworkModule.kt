package com.example.zalgneyhmusic.di

import com.example.zalgneyhmusic.data.model.api.AlbumDeserializer
import com.example.zalgneyhmusic.data.model.api.AlbumDTO
import com.example.zalgneyhmusic.data.model.api.ArtistDTO
import com.example.zalgneyhmusic.data.model.api.ArtistDeserializer
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
 * Dagger Hilt module for network dependencies.
 * Provides Gson, OkHttpClient, Retrofit, and API service.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for the backend API.
     * Note: consider moving to BuildConfig for environment-based switching.
     */
    private const val BASE_URL = "https://zalgneyh-backend-production.up.railway.app/api/"

    /**
     * Provides Gson instance for JSON (de)serialization.
     * - Registers AlbumDeserializer to handle flexible album.artist field
     *   (can be string id or full artist object).
     * - Registers ArtistDeserializer to handle flexible ArtistDTO mapping.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            // AlbumDTO custom deserializer
            .registerTypeAdapter(AlbumDTO::class.java, AlbumDeserializer())
            // ArtistDTO custom deserializer
            .registerTypeAdapter(ArtistDTO::class.java, ArtistDeserializer())
            .create()
    }

    /**
     * Provides OkHttpClient with HTTP logging and timeouts.
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
     * Provides Retrofit configured with the base URL, OkHttp client, and Gson converter.
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
     * Provides API service interface implementation.
     */
    @Provides
    @Singleton
    fun provideZalgneyhApiService(retrofit: Retrofit): ZalgneyhApiService {
        return retrofit.create(ZalgneyhApiService::class.java)
    }
}
