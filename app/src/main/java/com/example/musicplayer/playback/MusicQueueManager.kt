package com.example.musicplayer.playback

import com.example.musicplayer.api.ApiHelper
import com.example.musicplayer.home.Song

object MusicQueueManager {
    private val queue = mutableListOf<Song>()
    private var currentSong: Song? = null
    private var currentIndex = -1
    fun add(song: Song): Boolean {
        val exists = queue.any {it.id == song.id}
        if (exists){return false}
        queue.add(song)
        return true
    }

    fun getQueue(): List<Song> = queue
    fun getCurrent(): Song? = currentSong
    fun setCurrentSong(song: Song) {
        currentSong = song
        currentIndex = queue.indexOf(song)
    }
    fun playNext(): Song? {
        return if (currentIndex != -1 && currentIndex + 1 < queue.size) {
            currentIndex++
            currentSong = queue[currentIndex]
            currentSong
        } else null
    }
    fun playPrevious(): Song? {
        return if (currentIndex > 0) {
            currentIndex--
            currentSong = queue[currentIndex]
            currentSong
        } else null
    }
    fun remove(song: Song) {
        val removedIndex = queue.indexOf(song)
        if (removedIndex == -1) return
        queue.removeAt(removedIndex)
        if (song == currentSong) {
            if (queue.isNotEmpty()) {
                currentIndex = minOf(removedIndex, queue.size - 1)
                currentSong = queue[currentIndex]
            } else {
                currentSong = null
                currentIndex = -1
            }
        } else {
            if (removedIndex < currentIndex) {
                currentIndex--
            }
        }
    }
    fun getPlayableSong(song: Song, callback: (Song?) -> Unit) {
        val now = System.currentTimeMillis()
        val expired = (now - song.lastFetchTime) > 10 * 60 * 1000
        if (song.url.isEmpty() || expired) {
            ApiHelper.refreshPreview(song) { refreshed ->
                callback(refreshed)
            }
        } else {
            callback(song)
        }
    }
    fun removeQueue(callback: (Song?) -> Unit ) {
        queue.clear()
        currentSong = null
        currentIndex = -1
        callback(null)
    }
    fun getNextSong(): Song? {
        if (currentIndex == -1 || currentIndex + 1 >= queue.size) {
            return null
        }
        return queue[currentIndex + 1]
    }

    fun moveSongInQueue(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition ||fromPosition >= queue.size ||toPosition >= queue.size ||
            fromPosition < 0 || toPosition < 0) {
            return
        }
        val currentSongBeforeMove = getCurrent()
        val item = queue.removeAt(fromPosition)
        queue.add(toPosition, item)
        currentSongBeforeMove?.let{currentIndex = queue.indexOf(it)}
    }
}