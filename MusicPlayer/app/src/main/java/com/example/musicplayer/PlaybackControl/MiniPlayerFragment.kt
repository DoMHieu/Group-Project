package com.example.musicplayer.playbackcontrol

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.musicplayer.R
import androidx.fragment.app.activityViewModels
import android.widget.ImageView

class MiniPlayerFragment : Fragment(R.layout.fragment_mini_player) {
    private val playerViewModel: PlayerViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<View>(R.id.miniPlayerContainer)
        container.setOnClickListener {
            val albumArt = view.findViewById<ImageView>(R.id.albumArtImageView)
            playerViewModel.setFullScreenPlayerVisibility(true)
            openFullScreenPlayer(albumArt)
        }
    }
        private fun openFullScreenPlayer(startingView: View) {
            requireActivity().supportFragmentManager.commit {
                addSharedElement(startingView, "album_art_transition")
                add(R.id.full_player_container, PlayerFragment())
                addToBackStack(null)
            }
        }
    }