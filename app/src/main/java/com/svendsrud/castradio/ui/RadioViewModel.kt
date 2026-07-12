package com.svendsrud.castradio.ui

import android.app.Application
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.svendsrud.castradio.R
import com.svendsrud.castradio.model.RadioStation
import com.svendsrud.castradio.model.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val activeStation: RadioStation? = null,
    val isBuffering: Boolean = false,
    val castAvailable: Boolean = true,
    @StringRes val messageRes: Int? = null,
    val devicePickerRequested: Boolean = false
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PENDING_STATION_TIMEOUT_MS = 2 * 60 * 1000L
    }

    // Stasjonslisten må vises selv om Google Play Services / Cast-init feiler på enheten.
    private val castContext: CastContext? = try {
        CastContext.getSharedInstance(application)
    } catch (e: Exception) {
        null
    }

    private val _uiState = MutableStateFlow(
        RadioUiState(
            stations = StationRepository.stations,
            castAvailable = castContext != null,
            messageRes = if (castContext == null) R.string.cast_unavailable else null
        )
    )
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var pendingStation: RadioStation? = null
    private var pendingStationSetAtMs = 0L

    private val mediaCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val mediaStatus = currentSession()?.remoteMediaClient?.mediaStatus ?: return
            when (mediaStatus.playerState) {
                MediaStatus.PLAYER_STATE_BUFFERING -> setBuffering(true)
                MediaStatus.PLAYER_STATE_PLAYING -> setBuffering(false)
                MediaStatus.PLAYER_STATE_IDLE -> if (mediaStatus.idleReason == MediaStatus.IDLE_REASON_ERROR) {
                    _uiState.update {
                        it.copy(activeStation = null, isBuffering = false, messageRes = R.string.cast_playback_error)
                    }
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
            syncActiveStationFromSession(session)
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            session.remoteMediaClient?.unregisterCallback(mediaCallback)
            _uiState.update { it.copy(activeStation = null, isBuffering = false) }
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            _uiState.update { it.copy(messageRes = R.string.cast_connect_failed) }
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            _uiState.update { it.copy(messageRes = R.string.cast_connect_failed) }
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    init {
        castContext?.sessionManager?.let { manager ->
            manager.addSessionManagerListener(sessionListener, CastSession::class.java)
            val session = manager.currentCastSession
            session?.remoteMediaClient?.registerCallback(mediaCallback)
            // Re-sync i tilfelle en økt allerede var aktiv før ViewModel ble opprettet
            // (f.eks. app-prosessen ble gjenskapt mens Cast-økten levde videre).
            if (session != null && session.isConnected) {
                syncActiveStationFromSession(session)
            }
        }
    }

    override fun onCleared() {
        castContext?.sessionManager?.let { manager ->
            manager.currentCastSession?.remoteMediaClient?.unregisterCallback(mediaCallback)
            manager.removeSessionManagerListener(sessionListener, CastSession::class.java)
        }
    }

    fun onStationSelected(station: RadioStation) {
        val manager = castContext?.sessionManager
        if (manager == null) {
            _uiState.update { it.copy(messageRes = R.string.cast_unavailable) }
            return
        }
        val session = manager.currentCastSession
        if (session != null && session.isConnected) {
            loadOnSession(session, station)
        } else {
            pendingStation = station
            pendingStationSetAtMs = SystemClock.elapsedRealtime()
            _uiState.update { it.copy(devicePickerRequested = true) }
        }
    }

    fun stopCasting() {
        castContext?.sessionManager?.endCurrentSession(true)
        _uiState.update { it.copy(activeStation = null, isBuffering = false) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(messageRes = null) }
    }

    fun onDevicePickerLaunched() {
        _uiState.update { it.copy(devicePickerRequested = false) }
    }

    private fun currentSession(): CastSession? = castContext?.sessionManager?.currentCastSession

    private fun syncActiveStationFromSession(session: CastSession) {
        val mediaStatus = session.remoteMediaClient?.mediaStatus ?: return
        val station = StationRepository.stations.firstOrNull { it.streamUrl == mediaStatus.mediaInfo?.contentId }
        if (station != null) {
            _uiState.update {
                it.copy(
                    activeStation = station,
                    isBuffering = mediaStatus.playerState == MediaStatus.PLAYER_STATE_BUFFERING
                )
            }
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
        _uiState.update { it.copy(activeStation = station, isBuffering = true) }
    }

    private fun setBuffering(buffering: Boolean) {
        _uiState.update { it.copy(isBuffering = buffering) }
    }
}
