package com.example.musicplayer.StreamingData

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SoundCloudApi {
    @GET("search")
    suspend fun searchTrack(@Query("q") query: String): Response<List<SoundCloudTrack>>

    @GET("play/{id}")
    suspend fun getTrack(@Path("id") id: Long): Response<StreamResponse>
}
