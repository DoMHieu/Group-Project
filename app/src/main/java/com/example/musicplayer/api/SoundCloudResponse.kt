package com.example.musicplayer.api

class SoundCloudResponse : ArrayList<SoundCloudResponseItem>()

data class SoundCloudResponseItem(
    val access: String,
    val artwork_url: String,
    val available_country_codes: Any,
    val bpm: Any,
    val comment_count: Int,
    val commentable: Boolean,
    val created_at: String,
    val description: String,
    val download_count: Int,
    val download_url: Any,
    val downloadable: Boolean,
    val duration: Int,
    val embeddable_by: String,
    val favoritings_count: Int,
    val genre: String,
    val id: Long,
    val isrc: String,
    val key_signature: Any,
    val kind: String,
    val label_name: String,
    val license: String,
    val metadata_artist: String,
    val monetization_model: Any,
    val permalink_url: String,
    val playback_count: Int,
    val policy: Any,
    val purchase_title: Any,
    val purchase_url: String,
    val release: Any,
    val release_day: Int,
    val release_month: Int,
    val release_year: Int,
    val reposts_count: Int,
    val secret_uri: Any,
    val sharing: String,
    val stream_url: String,
    val streamable: Boolean,
    val tag_list: String,
    val title: String,
    val uri: String,
    val urn: String,
    val user: User,
    val user_favorite: Boolean,
    val user_playback_count: Int,
    val waveform_url: String
)

data class User(
    val avatar_url: String,
    val city: String,
    val comments_count: Int,
    val country: String,
    val created_at: String,
    val description: String,
    val discogs_name: Any,
    val first_name: String,
    val followers_count: Int,
    val followings_count: Int,
    val full_name: String,
    val id: Int,
    val kind: String,
    val last_modified: String,
    val last_name: String,
    val likes_count: Int,
    val myspace_name: Any,
    val online: Boolean,
    val permalink: String,
    val permalink_url: String,
    val plan: String,
    val playlist_count: Int,
    val public_favorites_count: Int,
    val reposts_count: Int,
    val subscriptions: List<Subscription>,
    val track_count: Int,
    val uri: String,
    val urn: String,
    val username: String,
    val website: String,
    val website_title: String
)

data class Subscription(
    val product: Product
)

data class Product(
    val id: String,
    val name: String
)