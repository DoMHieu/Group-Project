package com.example.musicplayer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_song_join",
    primaryKeys = ["playlistId", "id"], // Khóa chính kết hợp
    indices = [Index(value = ["id"])],
    foreignKeys = [
        ForeignKey(
            entity = UserPlaylist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE // Tự xóa liên kết khi xóa playlist
        ),
        ForeignKey(
            entity = FavouriteSong::class, // Bảng chứa các bài hát
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE // Tự xóa liên kết khi xóa bài hát
        )
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val id: Long // (id của bài hát, tên là 'id' để khớp với FavouriteSong)
)