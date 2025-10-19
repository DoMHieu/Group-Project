package com.example.musicplayer.playbackcontrol

import androidx.lifecycle.ViewModel
import com.example.musicplayer.StreamingData.SoundCloudTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {
    private val _isFullScreenPlayerVisible = MutableStateFlow(false)
    val isFullScreenPlayerVisible: StateFlow<Boolean> = _isFullScreenPlayerVisible
    fun setFullScreenPlayerVisibility(isVisible: Boolean) {
        _isFullScreenPlayerVisible.value = isVisible
    }
    private val _currentTrack = MutableStateFlow<SoundCloudTrack?>(null)
    val currentTrack: StateFlow<SoundCloudTrack?> = _currentTrack
    fun updateCurrentTrack(track: SoundCloudTrack?) {
        _currentTrack.value = track
    }
}