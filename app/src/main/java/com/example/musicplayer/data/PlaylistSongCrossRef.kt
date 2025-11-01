package com.example.musicplayer.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
@SuppressLint("SetTextI18n")
@Entity(
    tableName = "playlist_song_join",
    primaryKeys = ["playlistId", "id"],
    indices = [Index(value = ["id"])],
    foreignKeys = [
        ForeignKey(
            entity = UserPlaylist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FavouriteSong::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val id: Long
)