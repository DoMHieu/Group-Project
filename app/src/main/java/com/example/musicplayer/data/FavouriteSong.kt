package com.example.musicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.home.Song
@Entity(tableName = "favourite_songs")
data class FavouriteSong(
    @PrimaryKey
    val id: Long,
    val title: String,
    var url: String,
    val artist: String,
    val cover: String? = "",
    val coverXL: String? = "",
    var lastFetchTime: Long = 0
)
fun Song.toFavouriteSong(): FavouriteSong {
    return FavouriteSong(
        id = this.id,
        title = this.title,
        url = this.url,
        artist = this.artist,
        cover = this.cover,
        coverXL = this.coverXL,
        lastFetchTime = this.lastFetchTime
    )
}
fun FavouriteSong.toSong(): Song {
    return Song(
        id = this.id,
        title = this.title,
        url = this.url,
        artist = this.artist,
        cover = this.cover,
        coverXL = this.coverXL,
        lastFetchTime = this.lastFetchTime
    )
}