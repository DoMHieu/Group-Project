package com.example.musicplayer.data.remote

import com.example.musicplayer.data.model.StreamResponse
import com.example.musicplayer.data.model.SoundCloudTrack
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SoundCloudApi {
    @GET("search/tracks")
    suspend fun searchTrack(@Query("q") query: String): Response<List<SoundCloudTrack>>
    @GET("play/{id}")
    suspend fun getTrack(@Path("id") id: Long): Response<StreamResponse>
}