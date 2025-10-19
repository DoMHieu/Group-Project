package com.example.musicplayer.Home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQuery"
    }

    private val _searchQuery = MutableStateFlow(savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: "")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun search(query: String) {
        _searchQuery.value = query
        savedStateHandle[SEARCH_QUERY_KEY] = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            // --- Giả lập gọi API ---
            delay(1000)
            val fakeResults = listOf(
                "Bài hát '$query' 1",
                "Nghệ sĩ '$query' A",
                "Album '$query' X"
            )

            _searchResults.value = fakeResults
            _isLoading.value = false
        }
    }
}