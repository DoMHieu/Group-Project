package com.example.musicplayer.api

import com.example.musicplayer.home.Song
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiHelper {
    fun refreshPreview(song: Song, callback: (Song?) -> Unit) {
        RetrofitClient.api.getTrack(song.id).enqueue(object : Callback<SoundCloudResponseItem> {
            override fun onResponse(call: Call<SoundCloudResponseItem>, response: Response<SoundCloudResponseItem>) {
                if (response.isSuccessful) {
                    val track = response.body()
                    if (track != null && track.stream_url.isNotEmpty()) {
                        song.url = RetrofitClient.baseUrl + track.stream_url.removePrefix("/")
                        song.lastFetchTime = System.currentTimeMillis()
                        callback(song)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SoundCloudResponseItem>, t: Throwable) {
                callback(null)
            }
        })
    }
}

