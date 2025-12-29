/**
 * MainActivity.kt
 *
 * Main entry point for the Limitless Companion Android application.
 *
 * This activity manages:
 * - App navigation
 * - Runtime permissions
 * - Service lifecycle
 * - Compose UI setup
 *
 * Architecture: UI Layer - Activity (Clean Architecture)
 *
 * TODO(milestone-1): Implement permission request handling
 * TODO(milestone-1): Add service binding logic
 * TODO(milestone-2): Implement proper navigation with Navigation Compose
 * TODO(milestone-2): Add deep link handling
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limitless.companion.services.AudioCaptureService
import com.limitless.companion.ui.screens.RecordingScreen.RecordingScreen
import com.limitless.companion.ui.theme.LimitlessCompanionTheme
// import timber.log.Timber

/**
 * Main activity for the application.
 */
class MainActivity : ComponentActivity() {

    // Service binding
    private var audioCaptureService: AudioCaptureService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? AudioCaptureService.AudioCaptureBinder
            audioCaptureService = binder?.getService()
            serviceBound = true
            // Timber.d("Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioCaptureService = null
            serviceBound = false
            // Timber.d("Service disconnected")
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Timber.d("All permissions granted")
            // TODO(milestone-1): Handle permissions granted
        } else {
            // Timber.w("Some permissions denied")
            // TODO(milestone-1): Show rationale or navigate to settings
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        requestNecessaryPermissions()

        setContent {
            LimitlessCompanionTheme {
                LimitlessCompanionApp(
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to service
        val intent = Intent(this, AudioCaptureService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        // Unbind from service
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    /**
     * Requests all necessary permissions for the app.
     *
     * TODO(milestone-1): Add proper permission rationale
     */
    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        // Add permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check which permissions are needed
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Starts the recording service.
     */
    private fun startRecording() {
        audioCaptureService?.startRecording()
        // Timber.d("Started recording")
    }

    /**
     * Stops the recording service.
     */
    private fun stopRecording() {
        audioCaptureService?.stopRecording()
        // Timber.d("Stopped recording")
    }
}

/**
 * Main app composable with navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitlessCompanionApp(
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Screen.Recording.route, Screen.Search.route, Screen.Settings.route)) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to start destination to avoid building up back stack
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Recording.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Recording.route) {
                RecordingScreen(
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording
                )
            }

            composable(Screen.Search.route) {
                // TODO(milestone-1): Implement SearchScreen
                PlaceholderScreen("Search")
            }

            composable(Screen.Settings.route) {
                // TODO(milestone-1): Implement SettingsScreen
                PlaceholderScreen("Settings")
            }
        }
    }
}

/**
 * Placeholder screen for unimplemented screens.
 */
@Composable
fun PlaceholderScreen(name: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "$name Screen - Coming Soon",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

/**
 * Screen definitions for navigation.
 */
sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Recording : Screen("recording", "Record", Icons.Default.Mic)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Recording, Search, Settings)
    }
}