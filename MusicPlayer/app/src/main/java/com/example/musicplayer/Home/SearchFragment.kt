package com.example.musicplayer.home

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.playbackcontrol.PlayerViewModel
import com.example.musicplayer.song.SongAdapter
import android.util.Log

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        setupRecyclerView(recyclerView)

        // Nút Back để đóng màn hình tìm kiếm
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        searchInput.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(textView.text.toString())
                true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchResults.collect { songAdapter.submitList(it) }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { progressBar.isVisible = it }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchResults.collect { songs ->
                Log.d("SearchDebug", "Fragment: Nhận được danh sách ${songs.size} bài hát để cập nhật UI.")
                songAdapter.submitList(songs)
            }
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        songAdapter = SongAdapter { song -> onSongClicked(song) }
        recyclerView.adapter = songAdapter
    }

    private fun onSongClicked(song: Song) {
        // Cập nhật bài hát hiện tại và ra lệnh phát nhạc
        playerViewModel.updateCurrentTrack(song)
        // TODO: Gọi MusicService để phát nhạc
    }
}