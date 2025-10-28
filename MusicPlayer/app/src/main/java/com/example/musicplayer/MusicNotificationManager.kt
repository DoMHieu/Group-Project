package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.C
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicplayer.playback.MusicQueueManager

class MusicNotificationManager(private val service: MusicService) {

    private val CHANNEL_ID = "music_channel_id"
    internal val NOTIFICATION_ID = 1

    private var currentCoverBitmap: Bitmap? = null
    private var coverTarget: CustomTarget<Bitmap>? = null
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            try {
                service.exoPlayer.play()
            } catch (_: UninitializedPropertyAccessException) {
            }
            updateNotification()
            updatePlaybackState()
        }

        override fun onPause() {
            try {
                service.exoPlayer.pause()
            } catch (_: UninitializedPropertyAccessException) {
            }
            updateNotification()
            updatePlaybackState()
        }

        override fun onSkipToNext() {
            val next = MusicQueueManager.playNext()
            next?.let { service.playSong(it) }
        }

        override fun onSkipToPrevious() {
            val prev = MusicQueueManager.playPrevious()
            prev?.let { service.playSong(it) }
        }

        override fun onSeekTo(pos: Long) {
            try {
                service.exoPlayer.seekTo(pos)
            } catch (_: UninitializedPropertyAccessException) {
            }
            updatePlaybackState()
            service.sendProgressBroadcast()
        }
    }

    val mediaSession: MediaSessionCompat = MediaSessionCompat(service, "MusicService").apply {
        isActive = true
        setCallback(mediaSessionCallback)
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Channel",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Channel for music playback"
            setShowBadge(false)
        }
        val manager = service.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun getActionIntent(action: String): PendingIntent {
        val intent = Intent(service, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            service,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildNotification(): Notification {
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val isPlaying = try {
            service.exoPlayer.isPlaying
        } catch (_: UninitializedPropertyAccessException) {
            false
        }
        val title = service.currentTitle.ifEmpty { "Did you really choose a song?" }
        val artist = service.currentArtist.ifEmpty { "It's not that hard!" }
        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.music_note_24px)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(mediaStyle)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_previous_24px,
                    "Previous",
                    getActionIntent("PREVIOUS")
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (isPlaying) R.drawable.pause_24px else R.drawable.play,
                    if (isPlaying) "Pause" else "Play",
                    getActionIntent("TOGGLE_PLAY")
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_next_24px,
                    "Next",
                    getActionIntent("NEXT")
                )
            )

        currentCoverBitmap?.let { builder.setLargeIcon(it) }

        return builder.build()
    }

    fun updateNotification() {
        val notification = buildNotification()
        val manager = service.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun loadCoverArt(coverUrl: String) {
        currentCoverBitmap = null
        if (coverUrl.isNotBlank()) {
            Glide.with(service.applicationContext)
                .asBitmap()
                .load(coverUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        currentCoverBitmap = resource
                        updateMediaMetadata()
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        currentCoverBitmap = null
                        updateMediaMetadata()
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        currentCoverBitmap = null
                        updateMediaMetadata()
                    }
                })
        } else {
            updateMediaMetadata()
        }
    }

    fun updatePlaybackState() {
        val player = try {
            service.exoPlayer
        } catch (_: UninitializedPropertyAccessException) {
            return
        }
        val position = player.currentPosition
        val playbackSpeed = if (player.isPlaying) 1f else 0f
        val state = if (player.isPlaying)
            PlaybackStateCompat.STATE_PLAYING
        else
            PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, playbackSpeed, SystemClock.elapsedRealtime())
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setBufferedPosition(player.bufferedPosition)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }

    fun updateMediaMetadata() {
        val player = try {
            service.exoPlayer
        } catch (_: UninitializedPropertyAccessException) {
            return
        }

        val duration = if (player.duration == C.TIME_UNSET) 0L else player.duration

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, service.currentTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, service.currentArtist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

        currentCoverBitmap?.let {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    fun release() {
        coverTarget?.let { Glide.with(service.applicationContext).clear(it) }
        coverTarget = null
        currentCoverBitmap = null
        mediaSession.isActive = false
        mediaSession.release()
    }
}