package com.example.musicplayer.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
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
import android.os.Handler
import android.os.Looper

class SearchFragment : Fragment() {
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private val allKeywords = listOf("Hatsune Miku", "Summer Pockets", "Doriko",
        "Native Faith", "U.N.Owen was her", "Septette for the dead princess", "Touhou", "Monitoring",
        "deco*27", "PinocchioP")
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var currentSearchCall: Call<List<SoundCloudResponseItem>>? = null

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
        searchInput.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {searchRunnable?.let { searchHandler.removeCallbacks(it) }}
            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                val query= s.toString().trim()
                searchRunnable = Runnable {
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
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }})}

    override fun onDestroyView() {
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        currentSearchCall?.cancel()
        recyclerView.adapter = null
        suggestionsRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun setupResultsRecyclerView() {
        adapter = SongAdapter(
            songs,
            onClick = { song ->
                MusicQueueManager.getPlayableSong(song) { playable ->
<<<<<<< HEAD
                    if (!isAdded) return@getPlayableSong

                    if (playable != null) {
                        MusicQueueManager.add(playable)
                        MusicQueueManager.setCurrentSong(playable)
                        context?.let { ctx ->
                            MusicService.play(
                                playable.url,
                                ctx,
                                title = playable.title,
                                artist = playable.artist,
                                cover = playable.cover ?: "",
                                coverXL = playable.coverXL ?: ""
                            )
                        }
                    } else {
                        view?.let {
                            Snackbar.make(it, "Can't play this song", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onLongClick = { song ->
                view?.let {
                    MusicQueueManager.add(song)
                    Snackbar.make(it, "Queue added", Snackbar.LENGTH_SHORT).show()
=======
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
                    } else {
                        Snackbar.make(requireView(), "Can't play this song", Snackbar.LENGTH_LONG).show()
                    }
>>>>>>> c70cff6b3ce7844d91e5a4b74c7171d0add0846b
                }
            },
            onLongClick = { song ->
                MusicQueueManager.add(song)
                Snackbar.make(requireView(), "Queue added", Snackbar.LENGTH_SHORT).show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSuggestionsRecyclerView() {
        suggestionsAdapter = SuggestionsAdapter(emptyList()) { suggestion ->
            searchInput.setText(suggestion)
            searchInput.clearFocus()
            hideKeyboard()
            showSuggestions(false)
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            searchSongs(suggestion)
        }
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        suggestionsRecyclerView.adapter = suggestionsAdapter
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
        currentSearchCall?.cancel()
        currentSearchCall = RetrofitClient.api.searchTrack(keyword)
        currentSearchCall?.enqueue(object : Callback<List<SoundCloudResponseItem>> {
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
                        Snackbar.make(it, "Invalid", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<SoundCloudResponseItem>>, t: Throwable) {
                if (call.isCanceled) {
                    return
                }
                if (!isAdded) return
                t.printStackTrace()
                view?.let {
                    Snackbar.make(it, "Lỗi kết nối", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }
}