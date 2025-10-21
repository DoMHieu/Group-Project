package com.example.musicplayer.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SoundCloudApi {
    @GET("search/tracks")
    fun searchTrack(@Query("q") query: String): Call<List<SoundCloudResponseItem>>

    @GET("play/{id}")
    fun getTrack(@Path("id") id: Long): Call<SoundCloudResponseItem>
}
