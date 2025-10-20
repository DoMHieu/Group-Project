package com.example.musicplayer.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song // <-- Đảm bảo import đúng lớp Song

class SongAdapter(
    private val onClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback) { // <-- THAY ĐỔI 1

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.songTitle)
        private val artist: TextView = itemView.findViewById(R.id.songArtist)
        private val cover: ImageView = itemView.findViewById(R.id.songCover)

        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist
            Glide.with(itemView.context)
                .load(song.cover)
                .apply(
                    RequestOptions.bitmapTransform(
                        MultiTransformation(CenterCrop(), RoundedCorners(24))
                    )
                        .placeholder(R.drawable.queue_music_24px)
                        .error(R.drawable.play_arrow_24px)
                )
                .into(cover)

            itemView.setOnClickListener { onClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }
    companion object SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}