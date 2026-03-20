/**
 * MainActivity.kt
 * Main entry point. Binds to AudioCaptureService and hosts the full navigation
 * with functional Recording, Transcripts, and Settings screens.
 */

package com.limitless.companion

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limitless.companion.data.local.preferences.AppPreferences
import com.limitless.companion.services.AudioCaptureService
import com.limitless.companion.services.AudioSource
import com.limitless.companion.services.CaptureState
import com.limitless.companion.services.ModelState
import com.limitless.companion.ui.RecordingScreen
import com.limitless.companion.ui.SettingsScreen
import com.limitless.companion.ui.TranscriptListScreen
import com.limitless.companion.ui.TranscriptsViewModel
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

    private var audioCaptureService: AudioCaptureService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            audioCaptureService = (binder as? AudioCaptureService.AudioCaptureBinder)?.getService()
            serviceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            audioCaptureService = null
            serviceBound = false
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions result — recording will work once RECORD_AUDIO is granted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNecessaryPermissions()

        val prefs = AppPreferences(this)

        setContent {
            MaterialTheme {
                LimitlessApp(
                    prefs = prefs,
                    getCaptureState = { audioCaptureService?.captureState },
                    getAudioSource = { audioCaptureService?.audioSource },
                    getTranscriptCount = { audioCaptureService?.transcriptCount },
                    getWhisperReady = {
                        // Access modelState via reflection-free property — hoist via flow
                        @Suppress("USELESS_CAST")
                        (audioCaptureService != null) as Boolean
                    },
                    onStartRecording = { audioCaptureService?.startRecording() },
                    onStopRecording = { audioCaptureService?.stopRecording() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, AudioCaptureService::class.java)
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitlessApp(
    prefs: AppPreferences,
    getCaptureState: () -> StateFlow<CaptureState>?,
    getAudioSource: () -> StateFlow<AudioSource>?,
    getTranscriptCount: () -> StateFlow<Int>?,
    getWhisperReady: () -> Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Collect service state flows if available
    val captureState by (getCaptureState() ?: return).collectAsState()
    val audioSource by (getAudioSource() ?: kotlinx.coroutines.flow.MutableStateFlow(AudioSource.NONE)).collectAsState()
    val transcriptCount by (getTranscriptCount() ?: kotlinx.coroutines.flow.MutableStateFlow(0)).collectAsState()

    val vm: TranscriptsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple("record", "Record", Icons.Default.Mic),
                    Triple("transcripts", "Transcripts", Icons.Default.List),
                    Triple("settings", "Settings", Icons.Default.Settings)
                ).forEach { (route, label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "record",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("record") {
                RecordingScreen(
                    captureState = captureState,
                    audioSource = audioSource,
                    transcriptCount = transcriptCount,
                    whisperReady = getWhisperReady(),
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording
                )
            }
            composable("transcripts") {
                TranscriptListScreen(viewModel = vm)
            }
            composable("settings") {
                SettingsScreen(
                    transcriptCount = transcriptCount,
                    whisperReady = getWhisperReady(),
                    prefs = prefs
                )
            }
        }
    }
}