package com.example.musicplayer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.musicplayer.home.Song
import com.example.musicplayer.playback.MusicQueueManager
import androidx.media3.common.C

class MusicService : Service() {
    private val timerHandler = Handler(Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null
    override fun onBind(intent: Intent?): IBinder? = null

    internal lateinit var exoPlayer: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var notificationManager: MusicNotificationManager

    internal var currentTitle: String = ""
    internal var currentArtist: String = ""
    internal var currentCover: String = ""
    internal var coverXL: String = ""

    companion object {
        const val ACTION_SET_SLEEP_TIMER = "com.example.musicplayer.ACTION_SET_SLEEP_TIMER"
        const val EXTRA_TIMER_DURATION_MS = "com.example.musicplayer.EXTRA_TIMER_DURATION_MS"
        const val ACTION_SEEKBAR_UPDATE = "MUSIC_SEEKBAR_UPDATE"
        const val ACTION_REQUEST_UI_UPDATE = "com.example.musicplayer.ACTION_REQUEST_UI_UPDATE"

        fun play(
            url: String,
            context: Context,
            title: String = "",
            artist: String = "",
            cover: String = "",
            coverXL: String = ""
        ) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = "PLAY_URL"
                putExtra("URL", url)
                putExtra("TITLE", title)
                putExtra("ARTIST", artist)
                putExtra("COVER", cover)
                putExtra("COVER_XL", coverXL)
            }
            context.startForegroundService(intent)
        }

        fun next(context: Context) {
            context.startForegroundService(Intent(context, MusicService::class.java).apply { action = "NEXT" })
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (::exoPlayer.isInitialized) {
                if (::notificationManager.isInitialized) {
                    notificationManager.updatePlaybackState()
                }
                sendSeekbarUpdate()
            }
            handler.postDelayed(this, 1000)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        notificationManager = MusicNotificationManager(this)
        notificationManager.createNotificationChannel()

        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) handleSongEnded()
                if (::notificationManager.isInitialized) {
                    notificationManager.updateNotification()
                    notificationManager.updatePlaybackState()
                    notificationManager.updateMediaMetadata()
                }
                sendProgressBroadcast()
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (::notificationManager.isInitialized) {
                    notificationManager.updateNotification()
                    notificationManager.updatePlaybackState()
                }
                sendProgressBroadcast()
            }

            override fun onPositionDiscontinuity(reason: Int) {
                if (::notificationManager.isInitialized) {
                    notificationManager.updatePlaybackState()
                }
                sendSeekbarUpdate()
            }
        })

        if (::notificationManager.isInitialized) {
            startForeground(notificationManager.NOTIFICATION_ID, notificationManager.buildNotification())
        }

        handler.post(updateRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_SET_SLEEP_TIMER -> {
                    val durationMs = intent.getLongExtra(EXTRA_TIMER_DURATION_MS, 0L)
                    setSleepTimer(durationMs)
                }
                ACTION_REQUEST_UI_UPDATE -> {
                    sendProgressBroadcast()
                }
                "TOGGLE_PLAY" -> {
                    if (::exoPlayer.isInitialized) {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        if (::notificationManager.isInitialized) {
                            notificationManager.updateNotification()
                            notificationManager.updatePlaybackState()
                        }
                        sendProgressBroadcast()
                    }
                }

                "SEEK_TO" -> {
                    val position = intent.getLongExtra("SEEK_TO", 0L)
                    if (::exoPlayer.isInitialized) {
                        exoPlayer.seekTo(position)
                        if (::notificationManager.isInitialized) notificationManager.updatePlaybackState()
                        sendProgressBroadcast()
                    }
                }

                "TOGGLE_REPEAT" -> {
                    if (::exoPlayer.isInitialized) {
                        exoPlayer.repeatMode =
                            if (exoPlayer.repeatMode == ExoPlayer.REPEAT_MODE_ONE)
                                ExoPlayer.REPEAT_MODE_OFF
                            else
                                ExoPlayer.REPEAT_MODE_ONE
                        sendProgressBroadcast()
                    }
                }

                "PLAY_URL" -> {
                    val url = intent.getStringExtra("URL") ?: return START_NOT_STICKY
                    currentTitle = intent.getStringExtra("TITLE") ?: ""
                    currentArtist = intent.getStringExtra("ARTIST") ?: ""
                    currentCover = intent.getStringExtra("COVER") ?: ""
                    coverXL = intent.getStringExtra("COVER_XL") ?: ""
                    this.coverXL = coverXL
                    if (::notificationManager.isInitialized) {
                        notificationManager.loadCoverArt(coverXL)
                    }

                    if (::exoPlayer.isInitialized) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(url.toUri()))
                        exoPlayer.prepare()
                        exoPlayer.play()
                        if (::notificationManager.isInitialized) {
                            notificationManager.updateNotification()
                            notificationManager.updatePlaybackState()
                            notificationManager.updateMediaMetadata()
                        }
                        sendProgressBroadcast()
                    }
                }

                "NEXT" -> {
                    val next = MusicQueueManager.playNext()
                    next?.let { playSong(it) }
                }

                "PREVIOUS" -> {
                    val prev = MusicQueueManager.playPrevious()
                    prev?.let { playSong(it) }
                }

                "CLEAR_QUEUE" -> {
                    MusicQueueManager.removeQueue {
                        if (::exoPlayer.isInitialized) exoPlayer.stop()
                        sendBroadcast(Intent("QUEUE_CLEARED"))
                        if (::notificationManager.isInitialized) {
                            notificationManager.updateNotification()
                            notificationManager.updatePlaybackState()
                        }
                    }
                }
                "STOP" -> stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        if (::exoPlayer.isInitialized) exoPlayer.release()
        if (::notificationManager.isInitialized) {
            notificationManager.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun handleSongEnded() {
        val next = MusicQueueManager.playNext()
        if (next != null) {
            playSong(next)
        } else {
            if (::exoPlayer.isInitialized) {
                exoPlayer.seekTo(0)
                exoPlayer.pause()
            }
            if (::notificationManager.isInitialized) {
                notificationManager.updateNotification()
                notificationManager.updatePlaybackState()
            }
        }
    }

    internal fun playSong(song: Song) {
        MusicQueueManager.getPlayableSong(song) { refreshed ->
            refreshed?.let {
                MusicQueueManager.setCurrentSong(it)
                currentTitle = it.title
                currentArtist = it.artist
                currentCover = it.cover ?: ""
                coverXL = it.coverXL ?: ""

                if (::notificationManager.isInitialized) {
                    notificationManager.loadCoverArt(coverXL)
                }

                if (::exoPlayer.isInitialized) {
                    exoPlayer.setMediaItem(MediaItem.fromUri(it.url.toUri()))
                    exoPlayer.prepare()
                    exoPlayer.play()
                    if (::notificationManager.isInitialized) {
                        notificationManager.updateNotification()
                        notificationManager.updatePlaybackState()
                        notificationManager.updateMediaMetadata()
                    }
                    sendProgressBroadcast()
                }
            }
        }
    }

    internal fun sendProgressBroadcast() {
        if (::exoPlayer.isInitialized) {
            val isPlaying = exoPlayer.isPlaying
            val isRepeating = exoPlayer.repeatMode == ExoPlayer.REPEAT_MODE_ONE
            val intent = Intent("MUSIC_PROGRESS_UPDATE").apply {
                putExtra("isPlaying", isPlaying)
                putExtra("isRepeating", isRepeating)
                putExtra("title", currentTitle)
                putExtra("artist", currentArtist)
                putExtra("cover", currentCover)
                putExtra("cover_xl", coverXL)
            }
            sendBroadcast(intent)
        } else {
            val intent = Intent("MUSIC_PROGRESS_UPDATE").apply {
                putExtra("isPlaying", false)
                putExtra("isRepeating", false)
                putExtra("title", currentTitle)
                putExtra("artist", currentArtist)
                putExtra("cover", currentCover)
                putExtra("cover_xl", coverXL)
            }
            sendBroadcast(intent)
        }
    }

    private fun setSleepTimer(durationMs: Long) {
        sleepTimerRunnable?.let { timerHandler.removeCallbacks(it) }
        sleepTimerRunnable = null
        when {
            durationMs > 0 -> {
                sleepTimerRunnable = Runnable {
                    if (::exoPlayer.isInitialized && exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        if (::notificationManager.isInitialized) {
                            notificationManager.updateNotification()
                            notificationManager.updatePlaybackState()
                        }
                    }
                }
                timerHandler.postDelayed(sleepTimerRunnable!!, durationMs)
            }
            durationMs == -1L -> {
                if (::exoPlayer.isInitialized) {
                    exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                }
                sendProgressBroadcast()
            }
        }
    }

    private fun sendSeekbarUpdate() {
        if (::exoPlayer.isInitialized) {
            if (::notificationManager.isInitialized) notificationManager.updatePlaybackState()

            val duration = if (exoPlayer.duration == C.TIME_UNSET) 0L else exoPlayer.duration
            val position = exoPlayer.currentPosition
            val intent = Intent(ACTION_SEEKBAR_UPDATE).apply {
                putExtra("position", position)
                putExtra("duration", duration)
            }
            sendBroadcast(intent)
        }
    }
}