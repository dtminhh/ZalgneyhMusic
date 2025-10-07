package com.example.zalgneyhmusic.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // ⚠️ QUAN TRỌNG: Thay YOUR_IP bằng IP máy tính của bạn
    // Chạy: ipconfig (Windows) hoặc ifconfig (Mac/Linux)
    // VD: "http://192.168.1.100:3000/"
    private const val BASE_URL = "http://192.168.5.4:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: MusicApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApiService::class.java)
    }

    /**
     * Helper function để lấy stream URL cho ExoPlayer
     */
    fun getStreamUrl(songId: String): String {
        return "${BASE_URL}api/songs/stream/$songId"
    }

    /**
     * Helper function để lấy full image URL
     */
    fun getImageUrl(imagePath: String?): String? {
        return if (imagePath != null && !imagePath.startsWith("http")) {
            "$BASE_URL${imagePath.removePrefix("/")}"
        } else {
            imagePath
        }
    }
}

