package com.example.musicplayer.playlist

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val coverUrl: String?
) {
    companion object {
        const val FAVOURITES_PLAYLIST_ID = -1L
    }
}