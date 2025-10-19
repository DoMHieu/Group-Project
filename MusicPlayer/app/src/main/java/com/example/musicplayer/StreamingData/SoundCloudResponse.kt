package com.example.musicplayer.StreamingData

data class SoundCloudTrack(
    val id: Long,
    val title: String,
    val stream_url: String?,
    val artwork_url: String? = null,
    val user: SoundCloudUser,
    val duration: Long
)

data class SoundCloudUser(
    val username: String,
    val avatar_url: String? = null
)

data class StreamResponse(
    val id: Long,
    val title: String,
    val stream_url: String
)
