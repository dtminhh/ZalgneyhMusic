package com.example.zalgneyhmusic.service

import com.example.zalgneyhmusic.data.model.domain.Song
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

const val BASE_URL = "https://localhost:3000"
val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

val api: ZalgneyhApiService = retrofit.create(ZalgneyhApiService::class.java)

interface ZalgneyhApiService {

    @GET("/api/songs")
    suspend fun getSongs(): List<Song>

}