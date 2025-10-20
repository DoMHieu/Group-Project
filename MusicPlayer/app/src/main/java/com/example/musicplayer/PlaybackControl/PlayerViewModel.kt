package com.example.musicplayer.playbackcontrol

import androidx.lifecycle.ViewModel
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {

    private val _currentTrack = MutableStateFlow<Song?>(null)
    val currentTrack: StateFlow<Song?> = _currentTrack

    private val _isFullScreenPlayerVisible = MutableStateFlow(false)
    val isFullScreenPlayerVisible: StateFlow<Boolean> = _isFullScreenPlayerVisible
    fun updateCurrentTrack(track: Song?) {
        _currentTrack.value = track
    }
    fun setFullScreenPlayerVisibility(isVisible: Boolean) {
        _isFullScreenPlayerVisible.value = isVisible
    }
}