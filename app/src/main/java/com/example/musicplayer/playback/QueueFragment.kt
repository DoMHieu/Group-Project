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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
import com.example.musicplayer.home.SongAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class QueueFragment : BottomSheetDialogFragment() {
    private lateinit var tvQueueSongCount: TextView
    private lateinit var btnQueueShuffle: ImageButton
    private lateinit var rvQueue: RecyclerView
    private lateinit var queueAdapter: SongAdapter
    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!::queueAdapter.isInitialized) return
            val currentQueue = MusicQueueManager.getQueue()
            tvQueueSongCount.text = "${currentQueue.size} songs"
            if (queueAdapter.itemCount != currentQueue.size) {
                queueAdapter.updateQueueList(currentQueue.toMutableList())
            } else {
                queueAdapter.updateCurrentSong()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnQueueShuffle = view.findViewById(R.id.btnQueueShuffle)
        tvQueueSongCount = view.findViewById(R.id.tvQueueSongCount)
        val currentQueue = MusicQueueManager.getQueue()
        tvQueueSongCount.text = "${currentQueue.size} songs"
        rvQueue = view.findViewById(R.id.queueRecyclerView)
        rvQueue.layoutManager = LinearLayoutManager(requireContext())
        queueAdapter = SongAdapter(
            MusicQueueManager.getQueue().toMutableList(),
            onClick = { song ->
                SongAdapter.playSong(requireContext(), song)
            },
            onLongClick = {
            }
        )
        rvQueue.adapter = queueAdapter
        queueAdapter.notifyDataSetChanged()
        btnQueueShuffle.setOnClickListener {
            MusicQueueManager.shuffle()
            val newQueue = MusicQueueManager.getQueue()
            queueAdapter.updateQueueList(newQueue.toMutableList())
            rvQueue.scrollToPosition(0)
        }

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.bindingAdapterPosition
                    val toPosition = target.bindingAdapterPosition
                    if(fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                        return false
                    }
                    queueAdapter.onItemMove(fromPosition, toPosition)
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) return
                    val song = queueAdapter.getSongAt(position) ?: return
                    val isCurrent = (song == MusicQueueManager.getCurrent())
                    if (isCurrent) {
                        queueAdapter.notifyItemChanged(position)
                        return
                    }
                    tvQueueSongCount.text = "${MusicQueueManager.getQueue().size} songs"
                    MusicQueueManager.remove(song)
                    queueAdapter.notifyItemRemoved(position)
                }
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    val requestUiIntent = Intent(requireContext(), MusicService::class.java).apply {
                        action = MusicService.ACTION_REQUEST_UI_UPDATE
                    }
                    requireContext().startService(requestUiIntent)
                }
            })
        itemTouchHelper.attachToRecyclerView(rvQueue)
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("MUSIC_PROGRESS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(musicReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                musicReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }
    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(musicReceiver)
    }
}