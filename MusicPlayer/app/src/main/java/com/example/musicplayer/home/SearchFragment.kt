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

class SearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    private val randomKeywords = listOf("love","summer","dance","rock","deco*27","PinocchioP","acoustic","chill","happy","Hatsune Miku","AJR","Disco")
    private fun getRandomKeyword(): String {
        return randomKeywords.random()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.searchInput)
        recyclerView = view.findViewById(R.id.recyclerView)

        adapter = SongAdapter(songs) { song ->
            MusicQueueManager.add(song)
            Snackbar.make(requireView(), "Added to the queue", Snackbar.LENGTH_SHORT).show()
            if (MusicQueueManager.getQueue().size == 1) {
                MusicQueueManager.getPlayableSong(song) { playable ->
                    if (playable != null) {
                        MusicQueueManager.setCurrentSong(playable)
                        MusicService.Companion.play(
                            playable.url,
                            requireContext(),
                            title = playable.title,
                            artist = playable.artist,
                            cover = playable.cover?:"",
                            coverXL = playable.coverXL?:"",
                        )
                    } else {
                        Snackbar.make(requireView(), "Không thể phát bài này", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        val randomKey = getRandomKeyword()
        searchSongs(randomKey)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.isNotEmpty()) {
                    searchSongs(keyword)
                } else {
                    val randomKey = getRandomKeyword()
                    searchSongs(randomKey)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchSongs(keyword: String) {
        RetrofitClient.api.searchTrack(keyword).enqueue(object : Callback<List<SoundCloudResponseItem>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<List<SoundCloudResponseItem>>,
                response: Response<List<SoundCloudResponseItem>>
            ) {
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
                    Snackbar.make(requireView(), "Lỗi khi tìm kiếm", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SoundCloudResponseItem>>, t: Throwable) {
                t.printStackTrace()
                Snackbar.make(requireView(), "Lỗi kết nối", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}