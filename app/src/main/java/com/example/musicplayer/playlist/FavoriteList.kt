package com.example.musicplayer.playlist

import android.content.Context
import android.content.Intent
import com.example.musicplayer.data.AppDatabase
import com.example.musicplayer.data.toFavouriteSong
import com.example.musicplayer.data.toSong
import com.example.musicplayer.home.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FavoriteList {

    private var favouriteSongIds = mutableSetOf<Long>()
    private var favouriteSongsList = mutableListOf<Song>()

    const val ACTION_FAVOURITE_CHANGED = "com.example.musicplayer.ACTION_FAVOURITE_CHANGED"
    const val ACTION_FAVOURITES_LOADED = "com.example.musicplayer.ACTION_FAVOURITES_LOADED"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun load(context: Context) {
        scope.launch {
            val dao = AppDatabase.getDatabase(context).favouriteDao()
            val loadedSongs = dao.getAll().map { it.toSong() }
            favouriteSongsList = loadedSongs.toMutableList()
            favouriteSongIds = loadedSongs.map { it.id }.toMutableSet()
            val intent = Intent(ACTION_FAVOURITES_LOADED)
            context.sendBroadcast(intent)
        }
    }

    fun getFavouriteSongs(): List<Song> {
        return favouriteSongsList.toList()
    }

    fun isFavourite(songId: Long): Boolean {
        return favouriteSongIds.contains(songId)
    }

    fun toggleFavourite(song: Song, context: Context): Boolean {
        val isCurrentlyFavourite = isFavourite(song.id)
        val newState: Boolean
        val dao = AppDatabase.getDatabase(context).favouriteDao()

        if (isCurrentlyFavourite) {
            favouriteSongIds.remove(song.id)
            favouriteSongsList.removeAll { it.id == song.id }
            newState = false

            scope.launch {
                dao.delete(song.toFavouriteSong())
            }
        } else {
            favouriteSongIds.add(song.id)
            song.lastFetchTime = System.currentTimeMillis()
            favouriteSongsList.add(0, song)
            newState = true

            scope.launch {
                dao.insert(song.toFavouriteSong())
            }
        }

        val intent = Intent(ACTION_FAVOURITE_CHANGED).apply {
            putExtra("songId", song.id)
            putExtra("isFavourite", newState)
        }
        context.sendBroadcast(intent)
        return newState
    }
}