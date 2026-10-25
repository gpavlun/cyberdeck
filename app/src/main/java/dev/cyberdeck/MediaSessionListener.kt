
package dev.cyberdeck

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import android.util.Log

data class MediaInfo(
    val title: String = "",
    val artist: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val hasSession: Boolean = false
)

object MediaSessionBridge {
    var info by mutableStateOf(MediaInfo())
        private set

    var controller by mutableStateOf<MediaController?>(null)
        private set

    internal fun updateController(value: MediaController?) {
        controller = value
        updateInfo()
    }

    internal fun updateInfo() {
        val active = controller
        val metadata = active?.metadata
        val playback = active?.playbackState

        info = if (active == null) {
            MediaInfo()
        } else {
            MediaInfo(
                title = metadata
                    ?.getString(MediaMetadata.METADATA_KEY_TITLE)
                    .orEmpty(),
                artist = metadata
                    ?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    .orEmpty(),
                durationMs = metadata
                    ?.getLong(MediaMetadata.METADATA_KEY_DURATION)
                    ?: 0L,
                positionMs = playback?.position ?: 0L,
                isPlaying = playback?.state ==
                    PlaybackState.STATE_PLAYING,
                hasSession = true
            )
        }
    }
}

class MediaSessionListener : NotificationListenerService() {

    private lateinit var sessionManager: MediaSessionManager
    private var currentController: MediaController? = null

    private val controllerCallback =
        object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                MediaSessionBridge.updateInfo()
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                MediaSessionBridge.updateInfo()
            }

            override fun onSessionDestroyed() {
                refreshSessions()
            }
        }

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            Log.d(
                "CyberdeckMedia",
                "Active sessions changed: ${controllers?.size ?: 0}"
            )
            selectSession(controllers.orEmpty())
        }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.d("CyberdeckMedia", "Notification listener connected")

        sessionManager =
            getSystemService(MediaSessionManager::class.java)

        sessionManager.addOnActiveSessionsChangedListener(
            sessionsListener,
            ComponentName(this, MediaSessionListener::class.java)
        )

        refreshSessions()
    }


    private fun refreshSessions() {
        if (!::sessionManager.isInitialized) return

        val sessions = sessionManager.getActiveSessions(
            ComponentName(this, MediaSessionListener::class.java)
        )

        Log.d("CyberdeckMedia", "Active sessions found: ${sessions.size}")

        selectSession(sessions)
    }

    private fun selectSession(controllers: List<MediaController>) {
    Log.d(
        "CyberdeckMedia",
        "Sessions: ${
            controllers.joinToString {
                "${it.packageName}: ${it.metadata?.description}"
            }
        }"
    )


        val selected = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull {
            it.metadata != null
        } ?: controllers.firstOrNull()

        if (selected?.sessionToken == currentController?.sessionToken) {
            MediaSessionBridge.updateInfo()
            return
        }

        currentController?.unregisterCallback(controllerCallback)
        currentController = selected

        selected?.registerCallback(controllerCallback)
        MediaSessionBridge.updateController(selected)
    }

    override fun onListenerDisconnected() {
        if (::sessionManager.isInitialized) {
            sessionManager.removeOnActiveSessionsChangedListener(
                sessionsListener
            )
        }

        currentController?.unregisterCallback(controllerCallback)
        currentController = null
        MediaSessionBridge.updateController(null)

        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        currentController?.unregisterCallback(controllerCallback)
        currentController = null
        MediaSessionBridge.updateController(null)

        super.onDestroy()
    }
}
