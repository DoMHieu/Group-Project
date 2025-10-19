package com.example.musicplayer.StreamingData

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//RetrofitClient for SEND and GET data from API, in this case is deezer api
object RetrofitClient {
    const val baseUrl = "https://test-something-for-my-project.onrender.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SoundCloudApi = retrofit.create(SoundCloudApi::class.java)
}

