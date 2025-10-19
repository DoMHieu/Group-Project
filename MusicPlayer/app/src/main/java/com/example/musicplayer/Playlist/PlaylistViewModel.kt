package com.example.musicplayer.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {

    // State: Giữ danh sách các playlist
    private val _playlists = MutableStateFlow<List<String>>(emptyList())
    val playlists: StateFlow<List<String>> = _playlists

    // State: Giữ trạng thái đang tải
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Khối init sẽ được chạy ngay khi ViewModel được tạo
    init {
        loadPlaylists()
    }

    /**
     * Hàm để tải danh sách playlists.
     */
    private fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            // --- Giả lập gọi API hoặc đọc từ database ---
            delay(1500)
            val fakePlaylists = listOf(
                "Playlist Yêu thích",
                "Nhạc US-UK Hay Nhất",
                "V-Pop Tháng 10",
                "Điệp khúc mùa thu"
            )
            // ---------------------------------------------
            _playlists.value = fakePlaylists
            _isLoading.value = false
        }
    }
}