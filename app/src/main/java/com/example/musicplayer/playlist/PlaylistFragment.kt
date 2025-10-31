package com.example.musicplayer.playlist

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.example.musicplayer.data.AppDatabase
import com.example.musicplayer.data.UserPlaylist
import com.example.musicplayer.data.toSong
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.launch

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var playlistAdapter: PlaylistAdapter
    private val playlistList = mutableListOf<Playlist>()
    private val favouriteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadPlaylistsFromDb()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadPlaylistsFromDb()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(FavoriteList.ACTION_FAVOURITE_CHANGED)
            addAction(FavoriteList.ACTION_FAVOURITES_LOADED)
        }

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

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(favouriteReceiver)
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Playlists"
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(playlistList) { playlist ->
            navigateToPlaylistDetails(playlist.id, playlist.name)
        }
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        binding.playlistRecyclerView.adapter = playlistAdapter
        binding.playlistRecyclerView.layoutManager = layoutManager
    }

    private fun setupClickListeners() {
        binding.fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPlaylistsFromDb() {
        lifecycleScope.launch {
            val favouriteSongs = FavoriteList.getFavouriteSongs()
            val favPlaylist = Playlist(
                id = Playlist.FAVOURITES_PLAYLIST_ID,
                name = "Favourite Song",
                songCount = favouriteSongs.size,
                coverUrl = null
            )
            val dao = AppDatabase.getDatabase(requireContext()).playlistDao()
            val playlistsFromDb = dao.getAllPlaylistsWithSongs()
            val userPlaylists = playlistsFromDb.map { playlistWithSongs ->
                Playlist(
                    id = playlistWithSongs.playlist.playlistId,
                    name = playlistWithSongs.playlist.name,
                    songCount = playlistWithSongs.songs.size,
                    coverUrl = playlistWithSongs.songs.firstOrNull()?.toSong()?.cover
                )
            }
            playlistList.clear()
            playlistList.add(favPlaylist)
            playlistList.addAll(userPlaylists)

            if (::playlistAdapter.isInitialized) {
                playlistAdapter.notifyDataSetChanged()
            }
        }
    }
    private fun showCreatePlaylistDialog() {
        val context = requireContext()
        val editText = TextInputEditText(context)
        editText.hint = "Tên playlist"

        MaterialAlertDialogBuilder(context)
            .setTitle("Tạo playlist mới")
            .setView(editText)
            .setPositiveButton("Tạo") { dialog, _ ->
                val name = editText.text.toString().trim()
                if (name.isBlank()) {
                    Snackbar.make(binding.root, "Invalid name", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(context).playlistDao()
                    val existingPlaylist = dao.getPlaylistByName(name)
                    val isDuplicateOfFavourites = "Favourite".equals(name, ignoreCase = true)
                    if (existingPlaylist != null || isDuplicateOfFavourites) {
                        Snackbar.make(binding.root, "Playlist '$name' existed", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val newPlaylist = UserPlaylist(name = name)
                        dao.insertPlaylist(newPlaylist)
                        loadPlaylistsFromDb()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun navigateToPlaylistDetails(playlistId: Long, playlistName: String) {
        val detailsFragment = PlaylistDetailsFragment.newInstance(playlistId, playlistName)
        parentFragmentManager.beginTransaction()
            .add(R.id.container, detailsFragment)
            .hide(this)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}