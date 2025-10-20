package com.example.musicplayer.data.model

import com.google.gson.annotations.SerializedName

data class SoundCloudTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("stream_url") val stream_url: String?,
    @SerializedName("artwork_url") val artwork_url: String?,
    @SerializedName("user") val user: SoundCloudUser,
    @SerializedName("duration") val duration: Long,
    @SerializedName("metadata_artist") val metadata_artist: String?
)

data class SoundCloudUser(
    @SerializedName("username") val username: String
)

data class StreamResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("stream_url") val stream_url: String
)