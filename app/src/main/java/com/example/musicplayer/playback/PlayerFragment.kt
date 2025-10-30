package com.example.musicplayer.playback

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.SeekBar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import java.util.concurrent.TimeUnit
import com.google.android.material.snackbar.Snackbar
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
import android.view.MotionEvent
import android.animation.ObjectAnimator
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.view.VelocityTracker

class PlayerFragment : Fragment() {
    private var velocityTracker: VelocityTracker? = null
    private lateinit var slider: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var textTotalTime: TextView
    private lateinit var playPauseButton: ImageView
    private lateinit var repeatButton: ImageView
    private lateinit var coverImage: ImageView
    private var isUserSeeking = false
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarSubtitle: TextView
    private var initialTouchY: Float = 0f
    private var isDragging: Boolean = false
    private lateinit var upNextText: TextView
    private lateinit var sleepTimerButton: ImageButton
    private lateinit var playNext: LinearLayout
    private var currentSongTitle: String = ""
    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            val newTitle = intent?.getStringExtra("title") ?: ""
            val newArtist = intent?.getStringExtra("artist") ?: ""
            val newCoverUrlXL = intent?.getStringExtra("cover_xl") ?: ""
            if (newTitle.isNotEmpty() && newTitle != currentSongTitle) {
                currentSongTitle = newTitle
                toolbarTitle.text = newTitle
                toolbarSubtitle.text = newArtist
                if (newCoverUrlXL.isNotBlank()) {
                    Glide.with(requireContext())
                        .load(newCoverUrlXL)
                        .apply(
                            RequestOptions.bitmapTransform(
                                MultiTransformation(CenterCrop(), RoundedCorners(24))
                            )
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .placeholder(R.drawable.image_24px)
                                .error(R.drawable.image_24px)
                        )
                        .into(coverImage)
                } else {
                    coverImage.setImageResource(R.drawable.image_24px)
                }
            }
            updateUpNextText()
            val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
            playPauseButton.setImageResource(
                if (isPlaying) R.drawable.pause_24px else R.drawable.play
            )
            playPauseButton.tag = if (isPlaying) "pause" else "play"
            val isRepeating = intent?.getBooleanExtra("isRepeating", false) ?: false
            repeatButton.setImageResource(
                if (isRepeating) R.drawable.repeat_one_24px else R.drawable.repeat
            )
        }
    }
    private val seekbarReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val position = intent?.getLongExtra("position", 0L) ?: 0L
            val duration = intent?.getLongExtra("duration", 0L) ?: 0L

            if (!isUserSeeking && duration > 0) {
                if (slider.max != duration.toInt()) {
                    slider.max = duration.toInt()
                }
                slider.progress = position.toInt().coerceAtMost(duration.toInt())
            }
            textCurrentTime.text = formatTime(position)
            textTotalTime.text = if (duration > 0) formatTime(duration) else "--:--"
        }
    }

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        return view
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        upNextText = view.findViewById(R.id.text_up_next)
        sleepTimerButton = view.findViewById(R.id.button_sleep_timer)
        playNext = view.findViewById(R.id.up_next_bar);updateUpNextText()
        slider = view.findViewById(R.id.seekBar)
        textCurrentTime = view.findViewById(R.id.current_time)
        textTotalTime = view.findViewById(R.id.total_time)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        repeatButton = view.findViewById(R.id.repeatButton)
        coverImage = view.findViewById(R.id.imageView)
        toolbarTitle = view.findViewById(R.id.titleTextView)
        toolbarSubtitle = view.findViewById(R.id.artistTextView)
        playPauseButton.setOnClickListener { sendMusicCommand("TOGGLE_PLAY") }
        repeatButton.setOnClickListener { sendMusicCommand("TOGGLE_REPEAT") }
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                seekBar?.let { sendMusicCommand("SEEK_TO", it.progress.toLong()) }
            }
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) { textCurrentTime.text = formatTime(progress.toLong()) }
            }
        })

        val nextButton = view.findViewById<ImageView>(R.id.nextButton)
        val previousButton = view.findViewById<ImageView>(R.id.prevButton)
        nextButton.setOnClickListener {
            val next = MusicQueueManager.playNext()
            next?.let {song ->
                MusicQueueManager.getPlayableSong(song) {playable ->
                    if(playable != null) {
                        MusicService.play(
                            playable.url,
                            requireContext(),
                            title = playable.title,
                            artist = playable.artist,
                            cover = playable.cover?:"",
                            coverXL = playable.coverXL?:""
                        )
                    } else
                        Snackbar.make(requireView(), "Can't play next song", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        previousButton.setOnClickListener {
            val prev = MusicQueueManager.playPrevious()
            prev?.let { song ->
                MusicQueueManager.getPlayableSong(song) { playable ->
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
                        Snackbar.make(requireView(), "Invalid song", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        playNext.setOnClickListener {
            val queueFragment = QueueFragment()
            queueFragment.show(parentFragmentManager, "QueueFragmentTag")
        }
        val favourite = view.findViewById<AppCompatImageButton>(R.id.favourite)
        favourite.setOnClickListener {
            Snackbar.make(requireView(), "Not functionable right now, hold to delete queue", Snackbar.LENGTH_SHORT).show()
        }
        favourite.setOnLongClickListener {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = "CLEAR_QUEUE"
            }
            requireContext().startForegroundService(intent)
            Snackbar.make(requireView(), "Queue deleted", Snackbar.LENGTH_SHORT).show()
            true
        }
        val fullscreen = view.findViewById<RelativeLayout>(R.id.relativelayout)
        fullscreen.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchY = event.rawY
                    isDragging = false
                    velocityTracker?.clear()
                    velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                    velocityTracker?.addMovement(event)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)
                    val deltaY = event.rawY - initialTouchY
                    if (deltaY > 0) {
                        isDragging = true
                        view.translationY = deltaY
                        view.alpha = 1.0f - (deltaY / view.height.toFloat())
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        velocityTracker?.computeCurrentVelocity(1000)
                        val yVelocity = velocityTracker?.yVelocity ?: 0f
                        val velocityThreshold = 600
                        val distanceThreshold = view.height * 0.15f
                        val deltaY = view.translationY
                        if (deltaY > distanceThreshold || yVelocity > velocityThreshold) {
                            ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat()).apply {
                                duration = 200
                                start()
                            }
                            ObjectAnimator.ofFloat(view, "alpha", 0f).apply {
                                duration = 200
                                start()
                            }
                            parentFragmentManager.beginTransaction().hide(this).commit()
                        } else {
                            ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
                                duration = 200
                                start()
                            }
                            ObjectAnimator.ofFloat(view, "alpha", 1f).apply {
                                duration = 200
                                start()
                            }
                        }
                    } else {
                        v.performClick()
                    }

                    isDragging = false
                    velocityTracker?.recycle()
                    velocityTracker = null
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
                            duration = 200
                            start()
                        }
                        ObjectAnimator.ofFloat(view, "alpha", 1f).apply {
                            duration = 200
                            start()
                        }
                    }
                    isDragging = false
                    velocityTracker?.recycle()
                    velocityTracker = null
                    true
                }
                else -> false
            }
        }
        sleepTimerButton.setOnClickListener {
            showSleepTimerDialog()
        }
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
        val requestUiIntent = Intent(requireContext(), MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_UI_UPDATE
        }
        requireContext().startService(requestUiIntent)
        val seekbarFilter = IntentFilter(MusicService.ACTION_SEEKBAR_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(seekbarReceiver, seekbarFilter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                seekbarReceiver,
                seekbarFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(musicReceiver)
        requireContext().unregisterReceiver(seekbarReceiver)
    }

    private fun sendMusicCommand(action: String, seekTo: Long? = null) {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            this.action = action
            seekTo?.let { putExtra("SEEK_TO", it) }
        }
        requireContext().startForegroundService(intent)
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val currentView = view ?: return

        if (hidden) {
            currentView.translationY = 0f
            currentView.alpha = 1f
        } else {
            currentView.translationY = currentView.height.toFloat()
            currentView.alpha = 0f
            ObjectAnimator.ofFloat(currentView, "translationY", 0f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(currentView, "alpha", 1f).apply {
                duration = 300
                start()
            }
        }
    }

    fun dismissWithAnimation() {
        val currentView = view ?: return
        ObjectAnimator.ofFloat(currentView, "translationY", currentView.height.toFloat()).apply {
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(currentView, "alpha", 0f).apply {
            duration = 300
            doOnEnd {
                if (isAdded) {
                    parentFragmentManager.beginTransaction().hide(this@PlayerFragment).commit()
                }
            }
            start()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUpNextText() {
        val nextSong = MusicQueueManager.getNextSong()
        if (nextSong != null) {
            upNextText.text = getString(R.string.up_next_format, nextSong.title)
        } else {
            upNextText.text = "Empty Queue"
        }
    }

    private fun showSleepTimerDialog() {
        val context = requireContext()
        val options = arrayOf("5 mins","15 mins", "30 mins", "60 mins", "End Queue", "Timer off")
        val durationsMs = longArrayOf(5*60*1000L ,15 * 60 * 1000L, 30 * 60 * 1000L, 60 * 60 * 1000L, -1L, 0L)
        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Set Timer")
            .setItems(options) { dialog, which ->
                val selectedDurationMs = durationsMs[which]
                val timerIntent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.ACTION_SET_SLEEP_TIMER
                    putExtra(MusicService.EXTRA_TIMER_DURATION_MS, selectedDurationMs)
                }
                context.startService(timerIntent)
                if (selectedDurationMs > 0) {
                    Snackbar.make(requireView(), "Timer on: ${options[which]}", Snackbar.LENGTH_SHORT).show()
                } else if (selectedDurationMs == 0L) {
                    Snackbar.make(requireView(), "Timer Disabled", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(requireView(), "Automatic end after queue", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}