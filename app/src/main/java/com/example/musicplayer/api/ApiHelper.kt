package com.example.musicplayer.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.musicplayer.home.Song

object ApiHelper {
    private const val TAG = "ApiHelper"

    fun refreshPreview(song: Song, callback: (Song?) -> Unit) {
        RetrofitClient.api.getTrack(song.id).enqueue(object : Callback<SoundCloudResponseItem> {
            override fun onResponse(call: Call<SoundCloudResponseItem>, response: Response<SoundCloudResponseItem>) {
                if (response.isSuccessful) {
                    val track = response.body()
                    if (track != null && track.stream_url.isNotBlank()) {
                        val stream = track.stream_url.trim()
                        // Nếu stream bắt đầu bằng http/https dùng nguyên, nếu không thì ghép với baseUrl
                        val finalUrl = if (stream.startsWith("http://") || stream.startsWith("https://")) {
                            stream
                        } else {
                            RetrofitClient.baseUrl.trimEnd('/') + "/" + stream.removePrefix("/")
                        }
                        Log.d(TAG, "refreshPreview: got stream_url='$stream' finalUrl='$finalUrl'")
                        song.url = finalUrl
                        song.lastFetchTime = System.currentTimeMillis()
                        callback(song)
                    } else {
                        Log.d(TAG, "refreshPreview: no stream_url or empty for id=${song.id}")
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "refreshPreview: response not successful code=${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SoundCloudResponseItem>, t: Throwable) {
                Log.w(TAG, "refreshPreview: fail ${t.message}", t)
                callback(null)
            }
        })
    }
}