package com.limitless.companion.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limitless.companion.services.AudioSource
import com.limitless.companion.services.CaptureState

@Composable
fun RecordingScreen(
    captureState: CaptureState,
    audioSource: AudioSource,
    transcriptCount: Int,
    whisperReady: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isRecording = captureState == CaptureState.RECORDING

    val pulseColor by animateColorAsState(
        targetValue = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(600),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(pulseColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (audioSource == AudioSource.NONE) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Microphone",
                modifier = Modifier.size(48.dp),
                tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Status text
        Text(
            text = when (captureState) {
                CaptureState.IDLE -> "Idle"
                CaptureState.INITIALIZING -> "Initializing…"
                CaptureState.RECORDING -> "Recording"
                CaptureState.PAUSED -> "Paused"
                CaptureState.STOPPED -> "Stopped"
                CaptureState.ERROR -> "Error"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Audio source chip
        if (captureState == CaptureState.RECORDING || captureState == CaptureState.PAUSED) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = when (audioSource) {
                            AudioSource.BLUETOOTH -> "🎧 Bluetooth"
                            AudioSource.MICROPHONE -> "🎤 Device Mic"
                            AudioSource.NONE -> "No Source"
                        }
                    )
                }
            )
        }

        // Whisper model status
        if (!whisperReady) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
            Text(
                text = "Loading speech model…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        // Start/Stop button
        FilledTonalButton(
            onClick = { if (isRecording) onStopRecording() else onStartRecording() },
            enabled = whisperReady || !isRecording,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }

        Spacer(Modifier.height(16.dp))

        // Stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Transcripts saved", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "$transcriptCount",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
