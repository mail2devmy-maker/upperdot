package com.mail2dev.upperdot.ui.new_relationship_note

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.ui.components.*
import com.mail2dev.upperdot.ui.insights.ContactSummary
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import com.mail2dev.upperdot.utils.StorageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipNoteSheet(
    onDismiss: () -> Unit,
    onSave: (Long, String, String, List<String>, String?, Long?, Long) -> Unit,
    existingNote: NoteEntity? = null,
    contactSearchQuery: String,
    onContactSearchQueryChange: (String) -> Unit,
    searchedContacts: List<ContactSummary>,
    attachmentPaths: List<String>,
    onAddAttachment: (String) -> Unit,
    onRemoveAttachment: (Int) -> Unit,
    currencySymbol: String,
    isMediaCompressionEnabled: Boolean = true,
    initialContact: ContactSummary? = null,
    isContactLocked: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedContact by remember(initialContact) { mutableStateOf(initialContact) }
    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }
    
    // Picker State
    var showPickerOverlay by remember { mutableStateOf(false) }

    // Date and Time State
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val initialDateTime = remember(existingNote) {
        existingNote?.createdAt?.let { 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        } ?: LocalDateTime.now()
    }
    
    var selectedDate by remember(existingNote) { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember(existingNote) { mutableStateOf(initialDateTime.toLocalTime()) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Attachment State
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            coroutineScope.launch {
                val internalPath = StorageUtils.saveUriWithOptionalCompression(
                    context, 
                    it, 
                    isMediaCompressionEnabled
                )
                if (internalPath != null) {
                    onAddAttachment(internalPath)
                }
            }
        }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            coroutineScope.launch {
                val internalPath = StorageUtils.saveUriWithOptionalCompression(
                    context, 
                    tempCameraUri!!, 
                    isMediaCompressionEnabled
                )
                if (internalPath != null) {
                    onAddAttachment(internalPath)
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File.createTempFile("temp_camera_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Voice Recording & Playback State wired via clean decoupled AudioHandler
    val appInstance = context.applicationContext as com.mail2dev.upperdot.UpperDotApp
    val audioHandler = appInstance.audioHandler

    var voiceRecordingPath by remember(existingNote) { mutableStateOf<String?>(existingNote?.voiceRecordingPath) }
    
    val isRecording by audioHandler.isRecording.collectAsState()
    val isPlaying by audioHandler.isPlaying.collectAsState()
    val recordingDurationMs by audioHandler.recordingDurationMs.collectAsState()
    var playbackPosition by remember { mutableStateOf(0L) }
    val currentPlaybackPos by audioHandler.playbackPositionMs.collectAsState()
    val audioDuration by audioHandler.audioDurationMs.collectAsState()
    val amplitudes = audioHandler.amplitudes.collectAsState().value

    LaunchedEffect(currentPlaybackPos) {
        playbackPosition = currentPlaybackPos
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    datePickerState.selectedDateMillis?.let { 
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false 
                }) {
                    Text("OK", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Surface)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            text = { TimePicker(state = timePickerState) },
            containerColor = Surface
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingNote == null) "New Relationship Note" else "Edit Relationship Note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Contact Picker
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .then(if (!isContactLocked) Modifier.clickable { showPickerOverlay = !showPickerOverlay } else Modifier)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedContact?.fullName ?: "Select Contact (Mandatory)",
                            color = if (selectedContact != null) Color.White else TextSecondary,
                            fontSize = 14.sp
                        )
                        if (!isContactLocked) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(if (showPickerOverlay) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = TextSecondary)
                        }
                    }
                }

                if (showPickerOverlay && !isContactLocked) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            CompactSearchField(value = contactSearchQuery, onValueChange = onContactSearchQueryChange, placeholder = "Type to filter contacts...", modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.heightIn(max = 200.dp)) {
                                if (searchedContacts.isEmpty() && contactSearchQuery.isNotEmpty()) {
                                    Text("No contacts found", color = TextSecondary, modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else {
                                    LazyColumn {
                                        items(searchedContacts) { contact ->
                                            Text(
                                                text = contact.fullName,
                                                color = Color.White,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    selectedContact = contact
                                                    showPickerOverlay = false
                                                    onContactSearchQueryChange("")
                                                }.padding(16.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StitchTextField(value = title, onValueChange = { title = it }, placeholder = "Note Title", modifier = Modifier.fillMaxWidth())
            StitchTextField(value = content, onValueChange = { content = it }, placeholder = "Content", singleLine = false, minLines = 6, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp))

            // Date & Time
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.weight(1f).background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).clickable { showDatePicker = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = selectedDate.format(dateFormatter), color = Color.White, fontSize = 14.sp)
                }
                Row(modifier = Modifier.weight(1f).background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).clickable { showTimePicker = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = selectedTime.format(timeFormatter), color = Color.White, fontSize = 14.sp)
                }
            }

            // Attachment Preview
            if (attachmentPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Attachments (${attachmentPaths.size})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        itemsIndexed(attachmentPaths) { index, path ->
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(model = path, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(20.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).clickable { onRemoveAttachment(index) }) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (isRecording) {
                RecordingIndicator(durationMs = recordingDurationMs, amplitudes = amplitudes)
            }

            if (voiceRecordingPath != null && !isRecording) {
                val safePath = com.mail2dev.upperdot.utils.StorageUtils.getSafeAbsolutePath(context, voiceRecordingPath)
                VoiceMemoPlayerCard(
                    isPlaying = isPlaying,
                    positionMs = playbackPosition,
                    durationMs = audioDuration,
                    onTogglePlayback = { 
                        if (safePath != null) {
                            if (isPlaying) {
                                audioHandler.pausePlayback()
                            } else {
                                audioHandler.startPlayback(safePath)
                            }
                        }
                    },
                    onSeek = { 
                        playbackPosition = it
                        audioHandler.seekTo(it)
                    },
                    onDelete = {
                        audioHandler.stopPlayback()
                        try {
                            if (safePath != null) {
                                java.io.File(safePath).delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        voiceRecordingPath = null
                    }
                )
            }
            
            // Media Bar
            Surface(color = Surface, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Column {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { attachmentLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Image, null, tint = AccentCyan)
                            }
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val file = File.createTempFile("temp_camera_", ".jpg", context.cacheDir)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, null, tint = AccentCyan)
                            }
                        }
                        
                        Surface(
                            shape = CircleShape, 
                            color = if (isRecording) Color.Red.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.1f), 
                            modifier = Modifier
                                .size(48.dp)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        // Tactile Down: Start Recording
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            try {
                                                val folder = File(context.filesDir, "attachments")
                                                if (!folder.exists()) folder.mkdirs()
                                                val file = File(folder, "voice_note_${System.currentTimeMillis()}.mp4")
                                                voiceRecordingPath = file.absolutePath
                                                audioHandler.startRecording(file.absolutePath)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        
                                        // Wait for Lift or Cancel
                                        waitForUpOrCancellation()
                                        
                                        // Finalize on Release
                                        if (audioHandler.isRecording.value) {
                                            audioHandler.stopRecording()
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null, tint = if (isRecording) Color.Red else AccentCyan)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Professional Ergonomic Full-Width Button according to SRS spec
            Button(
                onClick = {
                    selectedContact?.let { contact ->
                        val combinedTimestamp = LocalDateTime.of(selectedDate, selectedTime)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        onSave(contact.id, title, content, attachmentPaths, voiceRecordingPath, existingNote?.id, combinedTimestamp)
                    }
                },
                enabled = selectedContact != null && title.isNotEmpty() && !isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Save Relationship Note",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
