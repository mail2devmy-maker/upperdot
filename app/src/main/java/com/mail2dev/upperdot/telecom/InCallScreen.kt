package com.mail2dev.upperdot.telecom

import android.telecom.Call as TelecomCall
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import com.mail2dev.upperdot.ui.components.TelephonyKeypad
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.NegativeRed
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.StitchDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InCallScreen(
    call: TelecomCall,
    state: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    showDialpad: Boolean,
    displayName: String? = null,
    onMuteClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onKeypadClick: () -> Unit,
    onDtmfPress: (Char) -> Unit,
    onDtmfRelease: () -> Unit,
    onAnswer: () -> Unit,
    onHangup: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    val handle = call.details.handle?.schemeSpecificPart ?: "Unknown"
    val formattedHandle = com.mail2dev.upperdot.util.ContactUtils.formatForDisplay(handle)
    val primaryText = displayName ?: formattedHandle
    val secondaryText = if (displayName != null) formattedHandle else null
    
    var dtmfDigits by remember { mutableStateOf("") }
    var showNoteSheet by remember { mutableStateOf(false) }
    var noteContent by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Caller Info with Standard Left-Aligned TopBar
            Column(modifier = Modifier.fillMaxWidth()) {
                StitchDesignSystem.TopBar(
                    title = "Active Call",
                    leadingIcon = Icons.Default.PhoneInTalk
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Avatar: Large Glow Avatar according to Reconstruction Spec
                if (!showDialpad) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        // Cyan Outer Glow Ring
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(AccentCyan.copy(alpha = 0.3f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = Surface,
                            border = androidx.compose.foundation.BorderStroke(2.dp, AccentCyan),
                            modifier = Modifier.size(120.dp).shadow(12.dp, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = primaryText.take(1).uppercase(),
                                    color = AccentCyan,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Caller Name & Status
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = primaryText,
                        color = Color.White,
                        fontSize = if (showDialpad) 24.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (secondaryText != null && !showDialpad) {
                        Text(
                            text = secondaryText,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = getCallStateText(state).uppercase(),
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Central Utility Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state != TelecomCall.STATE_RINGING) {
                    if (showDialpad) {
                        // DTMF Digits Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 24.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        ) {
                            Text(
                                text = dtmfDigits,
                                color = AccentCyan,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (dtmfDigits.isNotEmpty()) {
                                IconButton(onClick = { dtmfDigits = dtmfDigits.dropLast(1) }) {
                                    Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = Color.Gray)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        TelephonyKeypad(
                            onPressDown = { digit ->
                                onDtmfPress(digit)
                                dtmfDigits += digit
                            },
                            onPressUp = onDtmfRelease
                        )
                    } else {
                        // Anchored Action Grid
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallActionButton(
                                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = "Mute",
                                isActive = isMuted,
                                onClick = onMuteClick
                            )
                            CallActionButton(
                                icon = Icons.Default.NoteAdd,
                                label = "Quick Note",
                                onClick = { showNoteSheet = true }
                            )
                            CallActionButton(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                label = "Speaker",
                                isActive = isSpeakerOn,
                                onClick = onSpeakerClick
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // Bottom Primary Control Layer
                if (state == TelecomCall.STATE_RINGING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FloatingActionButton(
                            onClick = onHangup,
                            containerColor = NegativeRed,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(36.dp))
                        }

                        FloatingActionButton(
                            onClick = onAnswer,
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(Icons.Default.Call, null, modifier = Modifier.size(36.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Keypad Toggle
                        CallActionButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            isActive = showDialpad,
                            onClick = onKeypadClick
                        )

                        // Main End Call Button - Centered and Large
                        FloatingActionButton(
                            onClick = onHangup,
                            containerColor = NegativeRed,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(90.dp).shadow(16.dp, CircleShape)
                        ) {
                            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(44.dp))
                        }
                        
                        // Extra Spacer for alignment
                        Box(modifier = Modifier.width(80.dp))
                    }
                }
            }
        }

        if (showNoteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNoteSheet = false },
                sheetState = sheetState,
                containerColor = Surface,
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Quick Note - $primaryText",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        placeholder = { Text("Write a quick note...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = AccentCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (noteContent.isNotBlank()) {
                                onSaveNote(noteContent)
                                noteContent = ""
                                showNoteSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Note", fontWeight = FontWeight.Bold)
                    }
                }
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = if (isActive) Color.White else Surface,
            border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else (if (enabled) Color.White else Color.Gray),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (enabled) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
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
