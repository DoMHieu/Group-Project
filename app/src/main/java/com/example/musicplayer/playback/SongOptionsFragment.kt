package com.example.musicplayer.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentSongOptionsBinding
import com.example.musicplayer.home.Song
import com.example.musicplayer.playlist.FavoriteList
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
@SuppressLint("SetTextI18n")
class SongOptionsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSongOptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var song: Song
    private var isFavourite: Boolean = false
    private var isInQueue: Boolean = false
    private val favouriteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val changedSongId = intent?.getLongExtra("songId", -1L)
            if (changedSongId == song.id) {
                updateFavouriteState(FavoriteList.isFavourite(song.id))
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_SONG, Song::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_SONG) as? Song
        } ?: return

        bindData()
        setupClickListeners()
    }
    private fun bindData() {
        binding.optionSongTitle.text = song.title
        binding.optionSongArtist.text = song.artist
        Glide.with(this)
            .load(song.cover)
            .placeholder(R.drawable.ic_default_cover)
            .into(binding.optionSongCover)
        updateFavouriteState(FavoriteList.isFavourite(song.id))
        updateQueueState(MusicQueueManager.getQueue().any { it.id == song.id })
    }
    private fun setupClickListeners() {
        binding.optionBtnFavourite.setOnClickListener {
            FavoriteList.toggleFavourite(song, requireContext())
        }
        binding.optionBtnEnqueue.setOnClickListener {
            if (isInQueue) {
                MusicQueueManager.remove(song)
                updateQueueState(false)
            } else {
                MusicQueueManager.add(song)
                updateQueueState(true)
            }
        }

        binding.optionBtnAddPlaylist.setOnClickListener {
            showSnackbar("Under developing")
        }
    }
    private fun updateFavouriteState(isFav: Boolean) {
        this.isFavourite = isFav
        binding.optionBtnFavourite.setImageResource(
            if (isFav) R.drawable.favorite_checked else R.drawable.favorite_24px
        )
    }
    private fun updateQueueState(inQueue: Boolean) {
        this.isInQueue = inQueue
        val isCurrentSong = (song.id == MusicQueueManager.getCurrent()?.id)
        if (inQueue) {
            if(isCurrentSong) {
                binding.optionBtnEnqueue.visibility = View.GONE
            } else {
                binding.optionBtnEnqueue.visibility = View.VISIBLE
                binding.optionBtnEnqueue.text = "Dismiss queue"
                binding.optionBtnEnqueue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.playlist_remove_24px, 0, 0, 0)
            }
        } else {
            binding.optionBtnEnqueue.text = "Enqueue"
            binding.optionBtnEnqueue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.queue_music_24px, 0, 0, 0)
        }
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
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
    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(favouriteReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
    private fun showSnackbar(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SONG = "song_arg"

        fun newInstance(song: Song): SongOptionsFragment {
            return SongOptionsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SONG, song)
                }
            }
        }
    }
}