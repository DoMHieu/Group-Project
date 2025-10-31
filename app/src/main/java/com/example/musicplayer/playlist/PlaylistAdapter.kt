package com.example.musicplayer.playlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemPlaylistBinding
import android.widget.ImageView

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(playlist: Playlist) {
            binding.playlistTitle.text = playlist.name
            binding.playlistSongCount.text = "${playlist.songCount} bài hát"

            if (playlist.id == Playlist.FAVOURITES_PLAYLIST_ID) {
                Glide.with(binding.root.context).clear(binding.playlistCover)
                binding.playlistCover.setImageResource(R.drawable.favorite_24px)
                binding.playlistCover.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                Glide.with(binding.root.context)
                    .load(playlist.coverUrl ?: R.drawable.ic_default_cover)
                    .placeholder(R.drawable.ic_default_cover)
                    .into(binding.playlistCover)
            }
            binding.root.setOnClickListener {
                onClick(playlist)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size
}