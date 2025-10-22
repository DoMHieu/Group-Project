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
import android.animation.Animator
import androidx.core.animation.doOnEnd

class PlayerFragment : Fragment() {
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

    private val musicReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra("title") ?: ""
            val artist = intent?.getStringExtra("artist") ?: ""
            toolbarTitle.text = title
            toolbarSubtitle.text = artist
            val coverUrlXL = intent?.getStringExtra("cover_xl") ?: ""
            if (coverUrlXL.isNotBlank()) {
                Glide.with(requireContext())
                    .load(coverUrlXL)
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

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        return view
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slider = view.findViewById(R.id.progressSlider)
        textCurrentTime = view.findViewById(R.id.songCurrentProgress)
        textTotalTime = view.findViewById(R.id.songTotalTime)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        repeatButton = view.findViewById(R.id.repeatButton)
        coverImage = view.findViewById(R.id.imageView)
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        toolbarSubtitle = view.findViewById(R.id.toolbarSubtitle)
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
        val previousButton = view.findViewById<ImageView>(R.id.previousButton)
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

        val btnQueue = view.findViewById<AppCompatImageButton>(R.id.playlist_play)
        btnQueue.setOnClickListener {
            val queueFragment = QueueFragment()
            queueFragment.show(parentFragmentManager, "QueueFragmentTag")
        }
        btnQueue.setOnLongClickListener {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = "CLEAR_QUEUE"
            }
            requireContext().startForegroundService(intent)
            Snackbar.make(requireView(), "Queue deleted", Snackbar.LENGTH_SHORT).show()
            true
        }
        coverImage.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
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
                        val deltaY = view.translationY
                        val threshold = view.height / 3

                        if (deltaY > threshold) {
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
                        // Đây là một cú NHẤN (CLICK)
                        v.performClick()
                    }
                    isDragging = false
                    true
                }
                else -> false
            }
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
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(musicReceiver)
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
}