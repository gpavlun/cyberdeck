
package dev.cyberdeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import android.content.Intent
import android.provider.Settings

import dev.cyberdeck.ui.Main
import dev.cyberdeck.ui.theme.CyberdeckTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

import android.telecom.TelecomManager

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var homeResetKey by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat
                    .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            CyberdeckTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var systemUiVisible = false

                    val openMediaAccessSettings = {
                        startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        )
                    }


                    var callInProgress by remember { mutableStateOf(false) }

                    val telecomManager =
                        getSystemService(TELECOM_SERVICE) as TelecomManager

                    LaunchedEffect(Unit) {
                        while (true) {
                            callInProgress = try {
                                telecomManager.isInCall
                            } catch (_: SecurityException) {
                                false
                            }

                            delay(1_000)
                        }
                    }
                    



                    Main(
                        onToggleSystemUi = {
                            systemUiVisible = !systemUiVisible

                            val controller = WindowCompat.getInsetsController(
                                window,
                                window.decorView
                            )

                            if (systemUiVisible) {
                                controller.show(WindowInsetsCompat.Type.systemBars())
                            } else {
                                controller.hide(WindowInsetsCompat.Type.systemBars())
                            }
                        },
                        onOpenMediaAccessSettings = openMediaAccessSettings,
                        homeResetKey = homeResetKey,
                        callInProgress = callInProgress
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        if (
            intent.action == Intent.ACTION_MAIN &&
            intent.hasCategory(Intent.CATEGORY_HOME)
        ) {
            homeResetKey++
        }
    }

}

