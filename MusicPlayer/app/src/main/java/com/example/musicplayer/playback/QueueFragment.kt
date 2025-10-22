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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
import com.example.musicplayer.home.SongAdapter
import com.google.android.material.snackbar.Snackbar
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QueueFragment : BottomSheetDialogFragment() {

    private lateinit var rvQueue: RecyclerView
    private lateinit var queueAdapter: SongAdapter
    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (::queueAdapter.isInitialized) {
                queueAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvQueue = view.findViewById(R.id.queueRecyclerView)
        rvQueue.layoutManager = LinearLayoutManager(requireContext())
        queueAdapter = SongAdapter(
            MusicQueueManager.getQueue(),
            onClick = { song ->
                MusicQueueManager.getPlayableSong(song) { playable ->
                    if (playable != null) {
                        MusicQueueManager.setCurrentSong(playable)
                        MusicService.play(
                            playable.url,
                            requireContext(),
                            title = playable.title,
                            artist = playable.artist,
                            cover = playable.cover ?: "",
                            coverXL = playable.coverXL ?: ""
                        )
                        queueAdapter.notifyDataSetChanged()
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Invalid song, try delete from queue and retry!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onLongClick = {
            }
        )
        rvQueue.adapter = queueAdapter
        queueAdapter.notifyDataSetChanged()

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) return

                    val song = MusicQueueManager.getQueue()[position]
                    val isCurrent = (song == MusicQueueManager.getCurrent())
                    MusicQueueManager.remove(song)
                    queueAdapter.notifyItemRemoved(position)

                    if (isCurrent) {
                        val next = MusicQueueManager.getCurrent()
                        if (next != null) {
                            MusicQueueManager.getPlayableSong(next) { playable ->
                                if (playable != null) {
                                    MusicService.play(
                                        playable.url,
                                        requireContext(),
                                        title = playable.title,
                                        artist = playable.artist,
                                        cover = playable.cover ?: "",
                                        coverXL = playable.coverXL ?: "",
                                    )
                                } else
                                    Snackbar.make(
                                        requireView(),
                                        "Không thể phát bài kế tiếp",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                            }
                        } else {
                            MusicService.next(requireContext())
                        }
                    }
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