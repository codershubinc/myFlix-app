package com.codershubinc.myflix

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.core.net.toUri
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.extractor.DefaultExtractorsFactory


class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var errorTextView: TextView? = null

    private var loadingProgressBar: ProgressBar? = null

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

    @OptIn(UnstableApi::class) private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setRenderersFactory(DefaultRenderersFactory(this).setEnableDecoderFallback(true))
            .build().also { exoPlayer ->
            playerView?.player = exoPlayer

            val videoUri = getString(R.string.video_stream_uri).toUri()

            val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            httpDataSourceFactory.setDefaultRequestProperties(mapOf(getString(R.string.range_header_key) to getString(R.string.range_header_value)))

            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .setDrmConfiguration(null) // No DRM for this example
                .build()

            // Create a ProgressiveMediaSource with the configured httpDataSourceFactory and DefaultExtractorsFactory
            val mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory,
                DefaultExtractorsFactory()  ,
            )



                .createMediaSource(mediaItem)


            exoPlayer.setMediaSource(mediaSource)

            exoPlayer.prepare()
            exoPlayer.playWhenReady = true // Start playback automatically

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    super.onPlayerError(error)
                    playerView?.visibility = View.GONE
                    errorTextView?.visibility = View.VISIBLE
                    val errorMessage = getString(R.string.exoplayer_error_prefix) + error.message
                    print("playbackERR"     )
                    print( error)
                    errorTextView?.text = errorMessage
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        // In a multi-window environment, `onStart` may not be called, so ensure player is initialized
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}