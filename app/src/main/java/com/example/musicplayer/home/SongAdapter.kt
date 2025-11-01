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
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.playback.SongOptionsFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import android.util.Log
@SuppressLint("SetTextI18n")
class SongAdapter(
    private val items: MutableList<Song>,
    private val onClick: (Song) -> Unit,
    private val isQueueAdapter: Boolean = false,
    private val onDragStart: (RecyclerView.ViewHolder) -> Unit = { }
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private var currentSongId: Long? = MusicQueueManager.getCurrent()?.id
    private var isPlaying: Boolean = false
    companion object {

        fun playSong(context: Context, song: Song) {
            MusicQueueManager.getPlayableSong(song) { playableSong ->
                val appContext = context.applicationContext ?: return@getPlayableSong

                playableSong?.let {
                    MusicQueueManager.add(it)
                    MusicQueueManager.setCurrentSong(it)
                    MusicService.play(it.url,appContext,it.title,it.artist,it.cover ?: "",it.coverXL ?: "")
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
    @SuppressLint("ClickableViewAccessibility")
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.songTitle)
        val artist: TextView = itemView.findViewById(R.id.songArtist)
        val cover: ImageView = itemView.findViewById(R.id.songCover)
        val playingIcon: ImageView = itemView.findViewById(R.id.playingIcon)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
        val scrimOverlay: View = itemView.findViewById(R.id.scrim_overlay)
        val playPauseOverlay: ImageView = itemView.findViewById(R.id.play_pause_overlay)
        init {
            if (isQueueAdapter) {
                dragHandle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        onDragStart(this@SongViewHolder)
                    }
                    return@setOnTouchListener true
                }
            }
        }
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

        holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        holder.playingIcon.visibility = View.GONE

        if (isQueueAdapter) {
            holder.dragHandle.visibility = View.VISIBLE
            if (song.id == currentSongId) {
                holder.scrimOverlay.visibility = View.VISIBLE
                holder.playPauseOverlay.visibility = View.VISIBLE
                holder.playPauseOverlay.setImageResource(
                    if (isPlaying) R.drawable.pause_24px else R.drawable.play
                )
            } else {
                holder.scrimOverlay.visibility = View.GONE
                holder.playPauseOverlay.visibility = View.GONE
            }
        } else {
            holder.dragHandle.visibility = View.GONE
            holder.scrimOverlay.visibility = View.GONE
            holder.playPauseOverlay.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(song) }
        holder.itemView.setOnLongClickListener { view ->
            try {
                val fragmentManager = view.findFragment<Fragment>().parentFragmentManager
                SongOptionsFragment.newInstance(song)
                    .show(fragmentManager, "SongOptions")
            } catch (e: Exception) {
                val context = view.context
                if (context is AppCompatActivity) {
                    SongOptionsFragment.newInstance(song)
                        .show(context.supportFragmentManager, "SongOptions")
                } else {
                    Log.e("SongAdapter", "Can't find Fragment", e)
                }
            }
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
        this.isPlaying = true

        if (oldIndex != -1) {
            notifyItemChanged(oldIndex)
        }
        if (newIndex != -1) {
            notifyItemChanged(newIndex)
        }
    }
    fun updatePlaybackState(isPlaying: Boolean) {
        if (this.isPlaying == isPlaying) return
        this.isPlaying = isPlaying
        val currentIndex = items.indexOfFirst { it.id == currentSongId }
        if (currentIndex != -1) {
            notifyItemChanged(currentIndex)
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