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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
@SuppressLint("SetTextI18n")
class QueueFragment : BottomSheetDialogFragment() {
    private lateinit var tvQueueSongCount: TextView
    private lateinit var btnQueueShuffle: ImageButton
    private lateinit var rvQueue: RecyclerView
    private lateinit var queueAdapter: SongAdapter
    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!::queueAdapter.isInitialized) return
            val currentQueue = MusicQueueManager.getQueue()
            tvQueueSongCount.text = "${currentQueue.size} songs"
            if (queueAdapter.itemCount != currentQueue.size) {
                queueAdapter.updateQueueList(currentQueue.toMutableList())
            } else {
                queueAdapter.updateCurrentSong()
            }
            if (intent?.action == "MUSIC_PROGRESS_UPDATE") {
                val isPlaying = intent.getBooleanExtra("isPlaying", false)
                queueAdapter.updatePlaybackState(isPlaying)
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

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                override fun isLongPressDragEnabled(): Boolean = false
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
                    MusicQueueManager.remove(song)
                    queueAdapter.notifyItemRemoved(position)
                    tvQueueSongCount.text = "${MusicQueueManager.getQueue().size} songs"
                }
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    context?.let { safeContext ->
                        val requestUiIntent = Intent(safeContext, MusicService::class.java).apply {
                            action = MusicService.ACTION_REQUEST_UI_UPDATE
                        }
                        safeContext.startService(requestUiIntent)
                    }
                }
            })

        queueAdapter = SongAdapter(
            MusicQueueManager.getQueue().toMutableList(),
            onClick = { song ->
                val currentSong = MusicQueueManager.getCurrent()
                if (currentSong != null && song.id == currentSong.id) {
                    val toggleIntent = Intent(requireContext(), MusicService::class.java).apply {
                        action = "TOGGLE_PLAY"
                    }
                    requireContext().startService(toggleIntent)
                } else {
                    SongAdapter.playSong(requireContext(), song)
                }
            },
            isQueueAdapter = true,
            onDragStart = { viewHolder ->
                itemTouchHelper.startDrag(viewHolder)
            }
        )
        rvQueue.adapter = queueAdapter
        queueAdapter.notifyDataSetChanged()

        btnQueueShuffle.setOnClickListener {
            MusicQueueManager.shuffle()
            val newQueue = MusicQueueManager.getQueue()
            queueAdapter.updateQueueList(newQueue.toMutableList())
            rvQueue.scrollToPosition(0)
            tvQueueSongCount.text = "${newQueue.size} songs"
        }

        itemTouchHelper.attachToRecyclerView(rvQueue)
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        if(dialog!=null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if(bottomSheet!=null) {
                val layoutParams = bottomSheet.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                bottomSheet.layoutParams = layoutParams
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = true
                behavior.skipCollapsed = true
            }
        }
        val filter = IntentFilter("MUSIC_PROGRESS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(musicReceiver, filter, Context.RECEIVER_EXPORTED)
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
    }
    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(musicReceiver)
    }
}