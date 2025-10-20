package com.example.musicplayer.data.model

data class Song(
    val id: Long,
    val title: String,
    var url: String,
    val artist: String,
    val cover: String,
    val coverXL: String,
    val duration: Long,
    var lastFetchTime: Long = 0
)