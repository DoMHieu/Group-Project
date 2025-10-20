package com.example.musicplayer.data

import com.example.musicplayer.data.model.SoundCloudTrack
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.remote.RetrofitClient.api

class MusicRepository {
    private fun mapSoundCloudTrackToSong(track: SoundCloudTrack): Song {
        val largeArtworkUrl = track.artwork_url?.replace("-large.jpg", "-t500x500.jpg") ?: ""

        return Song(
            id = track.id,
            title = track.title,
            url = "",
            artist = track.metadata_artist ?: track.user.username,
            cover = track.artwork_url ?: "",
            coverXL = largeArtworkUrl,
            duration = track.duration
        )
    }

    suspend fun searchSongs(query: String): Result<List<Song>> {
        return try {
            val response = api.searchTrack(query)
            if (response.isSuccessful && response.body() != null) {
                val songList = response.body()!!.map { track ->
                    mapSoundCloudTrackToSong(track)
                }
                Result.success(songList)
            } else {
                Result.failure(Exception("API Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}