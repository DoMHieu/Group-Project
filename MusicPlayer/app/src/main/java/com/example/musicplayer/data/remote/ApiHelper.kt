package com.example.musicplayer.data

import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.remote.RetrofitClient

object ApiHelper {
    suspend fun refreshPreview(song: Song): Result<Song> {
        return try {
            val response = RetrofitClient.api.getTrack(song.id)
            if (response.isSuccessful) {
                val track = response.body()
                if (track != null && track.stream_url.isNotEmpty()) {
                    song.url = RetrofitClient.baseUrl + track.stream_url.removePrefix("/")
                    song.lastFetchTime = System.currentTimeMillis()
                    Result.success(song)
                } else {
                    Result.failure(Exception("API Error: Stream URL is null or empty."))
                }
            } else {
                Result.failure(Exception("API Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}