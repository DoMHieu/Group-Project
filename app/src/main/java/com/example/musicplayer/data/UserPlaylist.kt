package com.example.musicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_playlists")
data class UserPlaylist(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0L,
    val name: String
)