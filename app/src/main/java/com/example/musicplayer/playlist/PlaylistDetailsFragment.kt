package com.example.musicplayer.playlist

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import com.example.musicplayer.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.MusicService
import com.example.musicplayer.databinding.FragmentPlaylistDetailsBinding
import com.example.musicplayer.home.Song
import com.example.musicplayer.home.SongAdapter
import com.example.musicplayer.playback.MusicQueueManager
import com.google.android.material.snackbar.Snackbar

class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private var songList = mutableListOf<Song>()

    private var playlistId: Long = 0L
    private var playlistName: String = "Playlist"

    private val favouriteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (playlistId == Playlist.FAVOURITES_PLAYLIST_ID) {
                loadSongs()
            }
        }
    }
    private val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (::songAdapter.isInitialized) {
                songAdapter.updateCurrentSong()
            }
        }
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "playlist_id"
        private const val ARG_PLAYLIST_NAME = "playlist_name"

        fun newInstance(playlistId: Long, playlistName: String): PlaylistDetailsFragment {
            val fragment = PlaylistDetailsFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            args.putString(ARG_PLAYLIST_NAME, playlistName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong(ARG_PLAYLIST_ID)
            playlistName = it.getString(ARG_PLAYLIST_NAME) ?: "Playlist"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadSongs()
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        if (playlistId == Playlist.FAVOURITES_PLAYLIST_ID) {
            val filter = IntentFilter(FavoriteList.ACTION_FAVOURITE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(favouriteReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                ContextCompat.registerReceiver(
                    requireContext(),
                    favouriteReceiver,
                    filter,
                    null,
                    null,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            }
        }
        val musicFilter = IntentFilter("MUSIC_PROGRESS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(musicReceiver, musicFilter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                musicReceiver,
                musicFilter,
                null,
                null,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }
    override fun onStop() {
        super.onStop()
        if (playlistId == Playlist.FAVOURITES_PLAYLIST_ID) {
            requireContext().unregisterReceiver(favouriteReceiver)
        }
        requireContext().unregisterReceiver(musicReceiver)
    }

    private fun setupToolbar() {
        binding.toolbarDetails.title = playlistName
        binding.toolbarDetails.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songList,
            onClick = { song ->
                SongAdapter.playSong(requireContext(), song)
            },
            onLongClick = { song ->
                MusicQueueManager.add(song)
                Snackbar.make(binding.root, "Đã thêm vào hàng đợi", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.songsRecyclerView.adapter = songAdapter
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSongs() {
        songList.clear()

        if (playlistId == Playlist.FAVOURITES_PLAYLIST_ID) {
            songList.addAll(FavoriteList.getFavouriteSongs())
        } else {
            // TODO: (Sau này) load 'playlistId' từ database Room
        }

        if (::songAdapter.isInitialized) {
            songAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupClickListeners() {
        binding.playlistPlay.setOnClickListener {
            if (songList.isEmpty()) {
                return@setOnClickListener
            }
            val context = requireContext()
            val songsToPlay = songList.toList()
            MusicQueueManager.removeQueue {
                songsToPlay.forEach { MusicQueueManager.add(it) }
                val firstSong = songsToPlay[0]
                MusicQueueManager.setCurrentSong(firstSong)
                MusicQueueManager.getPlayableSong(firstSong) { playableSong ->
                    if (!isAdded || playableSong == null) return@getPlayableSong
                    MusicService.play(playableSong.url, context)
                    val uiUpdateIntent = Intent("MUSIC_PROGRESS_UPDATE").apply {
                        setPackage(context.packageName)
                        putExtra("title", playableSong.title)
                        putExtra("artist", playableSong.artist)
                        putExtra("cover", playableSong.cover)
                        putExtra("cover_xl", playableSong.coverXL)
                        putExtra("isPlaying", true)
                    }
                    context.sendBroadcast(uiUpdateIntent)
                }
            }

        }
    }
}