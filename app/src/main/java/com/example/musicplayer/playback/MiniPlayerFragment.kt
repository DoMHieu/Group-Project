package com.example.musicplayer.playback

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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
@SuppressLint("SetTextI18n")
class MiniPlayerFragment : Fragment() {

    private lateinit var cover: ImageView
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var playPause: ImageButton

    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentSong = MusicQueueManager.getCurrent()

            if (currentSong != null) {
                title.text = currentSong.title
                artist.text = currentSong.artist
                Glide.with(this@MiniPlayerFragment)
                    .load(currentSong.cover)
                    .placeholder(R.drawable.ic_default_cover)
                    .error(R.drawable.ic_default_cover)
                    .into(cover)
            } else {
                title.text = "Unknown"
                artist.text = "Unknown"
                Glide.with(this@MiniPlayerFragment).clear(cover)
            }
            val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
            playPause.setImageResource(
                if (isPlaying) R.drawable.pause_24px else R.drawable.play
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cover = view.findViewById(R.id.mini_player_cover)
        title = view.findViewById(R.id.mini_player_title)
        artist = view.findViewById(R.id.mini_player_artist)
        playPause = view.findViewById(R.id.mini_player_play_pause)

        playPause.setOnClickListener {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = "TOGGLE_PLAY"
            }
            requireContext().startService(intent)
        }
        view.setOnClickListener {
            val playerFragment = parentFragmentManager.findFragmentByTag("player") ?: return@setOnClickListener
            parentFragmentManager.beginTransaction()
                .show(playerFragment)
                .commit()
        }
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("MUSIC_PROGRESS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                musicReceiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                musicReceiver,
                filter,
                null,
                null,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        val requestUiIntent = Intent(requireContext(), MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_UI_UPDATE
        }
        requireContext().startService(requestUiIntent)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(musicReceiver)
    }
}