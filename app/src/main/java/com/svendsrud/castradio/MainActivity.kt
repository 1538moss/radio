package com.svendsrud.castradio

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.mediarouter.app.MediaRouteButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.svendsrud.castradio.model.RadioStation
import com.svendsrud.castradio.model.StationRepository
import com.svendsrud.castradio.ui.StationsAdapter

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PENDING_STATION_TIMEOUT_MS = 2 * 60 * 1000L
        private const val STATION_GRID_SPAN_COUNT = 2
    }

    private var castContext: CastContext? = null
    private lateinit var mediaRouteButton: MediaRouteButton
    private lateinit var adapter: StationsAdapter
    private lateinit var nowPlayingBar: View
    private lateinit var nowPlayingAvatar: ImageView
    private lateinit var nowPlayingBufferSpinner: ProgressBar
    private lateinit var nowPlayingLabel: TextView
    private lateinit var nowPlayingName: TextView
    private lateinit var stopCastingButton: ImageView
    private var pendingStation: RadioStation? = null
    private var pendingStationSetAtMs: Long = 0L

    private val mediaCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val mediaStatus = castContext?.sessionManager?.currentCastSession?.remoteMediaClient?.mediaStatus ?: return
            when (mediaStatus.playerState) {
                MediaStatus.PLAYER_STATE_BUFFERING -> setBuffering(true)
                MediaStatus.PLAYER_STATE_PLAYING -> setBuffering(false)
                MediaStatus.PLAYER_STATE_IDLE -> if (mediaStatus.idleReason == MediaStatus.IDLE_REASON_ERROR) {
                    Toast.makeText(this@MainActivity, R.string.cast_playback_error, Toast.LENGTH_SHORT).show()
                    setActiveStation(null)
                }
                else -> {}
            }
        }
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            session.remoteMediaClient?.registerCallback(mediaCallback)
            val station = pendingStation
            pendingStation = null
            // Only honor a pending pick if it's fresh — otherwise a cancelled device
            // picker followed by an unrelated later connection would replay a stale tap.
            if (station != null && SystemClock.elapsedRealtime() - pendingStationSetAtMs <= PENDING_STATION_TIMEOUT_MS) {
                loadOnSession(session, station)
            }
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            session.remoteMediaClient?.registerCallback(mediaCallback)
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            session.remoteMediaClient?.unregisterCallback(mediaCallback)
            setActiveStation(null)
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Toast.makeText(this@MainActivity, R.string.cast_connect_failed, Toast.LENGTH_SHORT).show()
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            Toast.makeText(this@MainActivity, R.string.cast_connect_failed, Toast.LENGTH_SHORT).show()
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Station list must render even if Google Play Services / Cast init fails on this device.
        adapter = StationsAdapter(StationRepository.stations) { station -> onStationTapped(station) }
        findViewById<RecyclerView>(R.id.stations_list).apply {
            layoutManager = GridLayoutManager(this@MainActivity, STATION_GRID_SPAN_COUNT)
            adapter = this@MainActivity.adapter
        }

        nowPlayingBar = findViewById(R.id.now_playing_bar)
        nowPlayingAvatar = findViewById(R.id.now_playing_avatar)
        nowPlayingBufferSpinner = findViewById(R.id.now_playing_buffer_spinner)
        nowPlayingLabel = findViewById(R.id.now_playing_label)
        nowPlayingName = findViewById(R.id.now_playing_name)
        stopCastingButton = findViewById(R.id.stop_casting_button)
        stopCastingButton.setOnClickListener {
            castContext?.sessionManager?.endCurrentSession(true)
            setActiveStation(null)
        }

        mediaRouteButton = findViewById(R.id.media_route_button)

        castContext = try {
            CastContext.getSharedInstance(this).also {
                CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton)
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.cast_unavailable, Toast.LENGTH_LONG).show()
            null
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionManager = castContext?.sessionManager
        sessionManager?.addSessionManagerListener(sessionListener, CastSession::class.java)
        val session = sessionManager?.currentCastSession
        session?.remoteMediaClient?.registerCallback(mediaCallback)
        // Re-sync the UI in case a session was already active before this Activity
        // instance existed (e.g. recreated on rotation) — otherwise the Now Playing
        // bar and active row stay hidden until the next unrelated status change.
        if (session != null && session.isConnected) {
            syncActiveStationFromSession(session)
        }
    }

    override fun onStop() {
        castContext?.sessionManager?.currentCastSession?.remoteMediaClient?.unregisterCallback(mediaCallback)
        castContext?.sessionManager?.removeSessionManagerListener(sessionListener, CastSession::class.java)
        super.onStop()
    }

    private fun onStationTapped(station: RadioStation) {
        val sessionManager = castContext?.sessionManager
        if (sessionManager == null) {
            Toast.makeText(this, R.string.cast_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val session = sessionManager.currentCastSession
        if (session != null && session.isConnected) {
            loadOnSession(session, station)
        } else {
            pendingStation = station
            pendingStationSetAtMs = SystemClock.elapsedRealtime()
            mediaRouteButton.performClick()
        }
    }

    private fun syncActiveStationFromSession(session: CastSession) {
        val mediaStatus = session.remoteMediaClient?.mediaStatus ?: return
        val station = StationRepository.stations.firstOrNull { it.streamUrl == mediaStatus.mediaInfo?.contentId }
        if (station != null) {
            setActiveStation(station, buffering = mediaStatus.playerState == MediaStatus.PLAYER_STATE_BUFFERING)
        }
    }

    private fun loadOnSession(session: CastSession, station: RadioStation) {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, station.name)
        }
        val mediaInfo = MediaInfo.Builder(station.streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType(station.contentType)
            .setMetadata(metadata)
            .build()
        val request = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build()
        session.remoteMediaClient?.load(request)
        setActiveStation(station, buffering = true)
    }

    private fun setActiveStation(station: RadioStation?, buffering: Boolean = false) {
        adapter.setActiveStation(station, buffering)
        if (station == null) {
            nowPlayingBar.visibility = View.GONE
        } else {
            nowPlayingAvatar.setImageResource(station.logoRes)
            nowPlayingName.text = station.name
            nowPlayingBar.visibility = View.VISIBLE
            updateNowPlayingBuffering(buffering)
        }
    }

    private fun setBuffering(buffering: Boolean) {
        adapter.setBuffering(buffering)
        if (nowPlayingBar.visibility == View.VISIBLE) {
            updateNowPlayingBuffering(buffering)
        }
    }

    private fun updateNowPlayingBuffering(buffering: Boolean) {
        nowPlayingLabel.text = getString(
            if (buffering) R.string.now_playing_buffering_label else R.string.now_playing_label
        )
        nowPlayingAvatar.visibility = if (buffering) View.INVISIBLE else View.VISIBLE
        nowPlayingBufferSpinner.visibility = if (buffering) View.VISIBLE else View.GONE
    }
}
