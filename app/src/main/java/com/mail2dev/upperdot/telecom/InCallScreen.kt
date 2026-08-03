package com.mail2dev.upperdot.telecom

import android.telecom.Call as TelecomCall
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.NegativeRed
import com.mail2dev.upperdot.ui.theme.Surface

@Composable
fun InCallScreen(
    call: TelecomCall,
    state: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    displayName: String? = null,
    onMuteClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onAnswer: () -> Unit,
    onHangup: () -> Unit,
    onAddNote: (String) -> Unit
) {
    val handle = call.details.handle?.schemeSpecificPart ?: "Unknown"
    val formattedHandle = com.mail2dev.upperdot.util.ContactUtils.formatForDisplay(handle)
    val primaryText = displayName ?: formattedHandle
    val secondaryText = if (displayName != null) formattedHandle else null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .align(Alignment.Center)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Surface,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = primaryText.take(1).uppercase(),
                                color = AccentCyan,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = primaryText,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (secondaryText != null) {
                        Text(
                            text = secondaryText,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = getCallStateText(state),
                        color = AccentCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Quick Note Button (Visible when Active)
                if (state == TelecomCall.STATE_ACTIVE) {
                    IconButton(
                        onClick = { onAddNote(handle) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp)
                            .size(56.dp)
                            .background(Surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Quick Note",
                            tint = AccentCyan
                        )
                    }
                }
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                if (state == TelecomCall.STATE_RINGING) {
                    // Answer / Decline Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Decline
                        FloatingActionButton(
                            onClick = onHangup,
                            containerColor = NegativeRed,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline")
                        }

                        // Answer
                        FloatingActionButton(
                            onClick = onAnswer,
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Answer")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mute
                        CallControlButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            isActive = isMuted,
                            onClick = onMuteClick
                        )

                        // Speaker
                        CallControlButton(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "Speaker",
                            isActive = isSpeakerOn,
                            onClick = onSpeakerClick
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // End Call
                    FloatingActionButton(
                        onClick = onHangup,
                        containerColor = NegativeRed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (isActive) Color.White else Surface,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

fun getCallStateText(state: Int): String {
    return when (state) {
        TelecomCall.STATE_ACTIVE -> "Active"
        TelecomCall.STATE_DIALING -> "Dialing..."
        TelecomCall.STATE_RINGING -> "Incoming Call"
        TelecomCall.STATE_CONNECTING -> "Connecting..."
        TelecomCall.STATE_DISCONNECTED -> "Disconnected"
        else -> "Calling..."
    }
}
