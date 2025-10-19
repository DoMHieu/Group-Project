package com.example.musicplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val SELECTED_TAB_KEY = "selected_tab_id"
    }
    private val _selectedTabId = MutableStateFlow(
        savedStateHandle.get<Int>(SELECTED_TAB_KEY) ?: R.id.home
    )
    val selectedTabId: StateFlow<Int> = _selectedTabId
    fun setSelectedTab(tabId: Int) {
        _selectedTabId.value = tabId
        savedStateHandle[SELECTED_TAB_KEY] = tabId
    }
}