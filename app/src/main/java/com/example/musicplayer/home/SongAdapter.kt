package com.example.musicplayer.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.playback.MusicQueueManager
import com.example.musicplayer.R
import com.example.musicplayer.MusicService
import android.content.Context
import android.content.Intent

class SongAdapter(
    private val items: MutableList<Song>,
    private val onClick: (Song) -> Unit,
    private val onLongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var currentSongId: Long? = MusicQueueManager.getCurrent()?.id
    companion object {

        fun playSong(context: Context, song: Song) {
            MusicQueueManager.getPlayableSong(song) { playableSong ->
                val appContext = context.applicationContext ?: return@getPlayableSong

                playableSong?.let {
                    MusicQueueManager.add(it)
                    MusicQueueManager.setCurrentSong(it)
                    MusicService.play(
                        it.url,
                        appContext,
                        it.title,
                        it.artist,
                        it.cover ?: "",
                        it.coverXL ?: ""
                    )
                    val uiUpdateIntent = Intent("MUSIC_PROGRESS_UPDATE").apply {
                        setPackage(appContext.packageName)
                        putExtra("title", it.title)
                        putExtra("artist", it.artist)
                        putExtra("cover", it.cover)
                        putExtra("cover_xl", it.coverXL)
                        putExtra("isPlaying", true)
                    }
                    appContext.sendBroadcast(uiUpdateIntent)
                }
            }
        }
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.songTitle)
        val artist: TextView = itemView.findViewById(R.id.songArtist)
        val cover: ImageView = itemView.findViewById(R.id.songCover)
        val playingIcon: ImageView = itemView.findViewById(R.id.playingIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = items[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        Glide.with(holder.itemView.context)
            .load(song.cover)
            .apply(
                RequestOptions.bitmapTransform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(24)
                    )
                )
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .placeholder(R.drawable.image_24px)
                    .error(R.drawable.image_24px)
            )
            .into(holder.cover)

        if (currentSongId != null && currentSongId == song.id) {
            holder.itemView.setBackgroundResource(R.drawable.playlist_current_play)
            holder.playingIcon.visibility = View.VISIBLE
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.playingIcon.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(song) }
        holder.itemView.setOnLongClickListener {
            onLongClick(song)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        MusicQueueManager.moveSongInQueue(fromPosition, toPosition)
    }
    fun getSongAt(position: Int): Song? {
        if (position >= 0 && position < items.size) {
            return items[position]
        }
        return null
    }

    fun updateCurrentSong() {
        val newCurrentSong = MusicQueueManager.getCurrent()
        val newId = newCurrentSong?.id
        val oldId = currentSongId

        if (newId == oldId) return

        val oldIndex = items.indexOfFirst { it.id == oldId }
        val newIndex = items.indexOfFirst { it.id == newId }

        currentSongId = newId

        if (oldIndex != -1) {
            notifyItemChanged(oldIndex)
        }
        if (newIndex != -1) {
            notifyItemChanged(newIndex)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateQueueList(newList: List<Song>) {
        items.clear()
        items.addAll(newList)
        currentSongId = MusicQueueManager.getCurrent()?.id
        notifyDataSetChanged()
    }
}