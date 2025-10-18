package com.codershubinc.myflix

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Create your Player and MediaSession in onCreate
    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        // Set the session activity for the MediaSession
        val sessionActivityPendingIntent = 
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    // Return a MediaSession object here. This is required.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // Release the player and media session in onDestroy
    override fun onDestroy() {
        player?.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}