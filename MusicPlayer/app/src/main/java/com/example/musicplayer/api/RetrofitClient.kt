package com.example.musicplayer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    const val baseUrl = "https://test-something-for-my-project.onrender.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SoundCloudApi = retrofit.create(SoundCloudApi::class.java)
}

