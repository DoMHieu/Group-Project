package com.example.musicplayer.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import com.example.musicplayer.playback.PlayerFragment
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import com.example.musicplayer.playback.MusicQueueManager
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
import com.example.musicplayer.api.RetrofitClient
import com.example.musicplayer.api.SoundCloudResponseItem
import com.google.android.material.snackbar.Snackbar
import android.view.inputmethod.InputMethodManager
import android.content.Context

class SearchFragment : Fragment() {
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private val allKeywords = listOf("love story", "summer vibe", "dance monkey",
        "rockstar", "acoustic covers", "lofi chill", "happy songs", "electronic music",
        "deco*27", "PinocchioP")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchInput = view.findViewById(R.id.searchInput)
        recyclerView = view.findViewById(R.id.recyclerView)
        suggestionsRecyclerView = view.findViewById(R.id.suggestionsRecyclerView)
        setupResultsRecyclerView()
        setupSuggestionsRecyclerView()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        searchInput.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    showSuggestions(false)
                    hideKeyboard()
                    searchInput.clearFocus()
                    searchSongs(query)
                }
                true
            } else {
                false
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                val query= s.toString().trim()
                if(query.isNotEmpty()){
                    val filteredSuggestions = allKeywords.filter { it.contains(query,ignoreCase = true) }
                    suggestionsAdapter.updateSuggestions(filteredSuggestions)
                    showSuggestions(true)
                }
                else {
                    showSuggestions(false)
                    songs.clear()
                    adapter.notifyDataSetChanged()
                }
            }})}

    private fun setupResultsRecyclerView() {
        adapter = SongAdapter(songs) { song ->
            MusicQueueManager.getPlayableSong(song) { playable ->
                if (playable != null) {
                    MusicQueueManager.add(playable)
                    MusicQueueManager.setCurrentSong(playable)
                    MusicService.play(
                        playable.url,
                        requireContext(),
                        title = playable.title,
                        artist = playable.artist,
                        cover = playable.cover ?: "",
                        coverXL = playable.coverXL ?: ""
                    )
                    parentFragmentManager.beginTransaction()
                        .add(R.id.container, PlayerFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Snackbar.make(requireView(), "Không thể phát bài hát này", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSuggestionsRecyclerView() {
        suggestionsAdapter= SuggestionsAdapter(emptyList()) {
            suggestion ->
            searchInput.setText(suggestion)
            searchInput.clearFocus()
            hideKeyboard()
            showSuggestions(false)
            searchSongs(suggestion)
        }
    }
    private fun showSuggestions(show: Boolean) {
        suggestionsRecyclerView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
    private fun searchSongs(keyword: String) {
        RetrofitClient.api.searchTrack(keyword).enqueue(object : Callback<List<SoundCloudResponseItem>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<List<SoundCloudResponseItem>>,
                response: Response<List<SoundCloudResponseItem>>
            ) {
                if (!isAdded) {
                    return
                }
                if (response.isSuccessful) {
                    val tracks = response.body() ?: emptyList()
                    songs.clear()
                    songs.addAll(tracks.map { item ->
                        val largeArtworkUrl = item.artwork_url?.replace("-large.jpg", "-t500x500.jpg")?: ""
                        Song(
                            id = item.id,
                            title = item.title,
                            url = "",
                            artist = item.metadata_artist?.takeIf { it.isNotBlank() } ?: item.user.username.ifEmpty { item.user.full_name.ifEmpty { "Unknown" } },
                            cover = item.artwork_url,
                            coverXL = largeArtworkUrl,
                            lastFetchTime = 0L
                        )
                    })

                    adapter.notifyDataSetChanged()
                } else {
                    view?.let {
                        Snackbar.make(it, "Lỗi khi tìm kiếm", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<SoundCloudResponseItem>>, t: Throwable) {
                t.printStackTrace()
                Snackbar.make(requireView(), "Lỗi kết nối", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}