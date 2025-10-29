package com.codershubinc.myflix

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var errorTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerView = findViewById(R.id.playerView)
        errorTextView = findViewById(R.id.errorTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        // 1. Create a RenderersFactory that prefers the FFmpeg extension
        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        // 2. Build the ExoPlayer instance using the custom RenderersFactory
        player = ExoPlayer.Builder(this, renderersFactory).build().also { exoPlayer ->
            playerView.player = exoPlayer

            // 3. Create a MediaItem from your video URL
            val videoUri = getString(R.string.video_stream_uri).toUri()
            val mediaItem = MediaItem.fromUri(videoUri)

            // 4. Set the media item and prepare the player. No need to manually specify an extractor.
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()

            // Add a listener to handle player states and errors
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> loadingProgressBar.visibility = View.VISIBLE
                        Player.STATE_READY, Player.STATE_ENDED -> loadingProgressBar.visibility = View.GONE
                        else -> { /* Do nothing */ }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    playerView.visibility = View.GONE
                    loadingProgressBar.visibility = View.GONE
                    errorTextView.visibility = View.VISIBLE
                    val errorMessage = getString(R.string.exoplayer_error_prefix) + error.message
                    errorTextView.text = errorMessage
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    // --- Player Lifecycle Management ---
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
}