package com.mail2dev.upperdot.ui.new_relationship_note

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.ui.components.CompactSearchField
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.insights.ContactSummary
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.media.MediaRecorder
import android.media.MediaPlayer
import java.io.File
import android.os.Build
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import com.mail2dev.upperdot.utils.StorageUtils
import coil.compose.AsyncImage
import androidx.core.content.FileProvider
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding

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
    initialContact: ContactSummary? = null,
    isContactLocked: Boolean = false
) {
    val context = LocalContext.current
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

    // Attachment State
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            val internalPath = StorageUtils.copyUriToInternalStorage(context, it)
            if (internalPath != null) {
                onAddAttachment(internalPath)
            }
        }
    }

    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                val internalPath = StorageUtils.copyUriToInternalStorage(context, uri)
                if (internalPath != null) {
                    onAddAttachment(internalPath)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File.createTempFile("temp_camera_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Voice Recording State
    var voiceRecordingPath by remember(existingNote) { mutableStateOf<String?>(existingNote?.voiceRecordingPath) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingDurationMs by remember { mutableLongStateOf(0L) }
    val amplitudes = remember { mutableStateListOf<Float>() }

    // Audio Playback State
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var audioDuration by remember { mutableLongStateOf(0L) }

    // Initialize duration when path is available (for existing notes)
    LaunchedEffect(voiceRecordingPath) {
        if (voiceRecordingPath != null && !isRecording) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(voiceRecordingPath)
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                val duration = time?.toLongOrNull() ?: 0L
                if (duration > 0) audioDuration = duration
            } catch (_: Exception) { }
        }
        playbackPosition = 0L
    }

    // Coroutine scope for timer and amplitude polling
    val recordingScope = rememberCoroutineScope()

    fun startRecording() {
        try {
            val folder = File(context.filesDir, "attachments")
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "voice_note_${System.currentTimeMillis()}.mp4")
            
            @Suppress("DEPRECATION")
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            
            recorder = newRecorder
            voiceRecordingPath = file.absolutePath
            isRecording = true
            recordingDurationMs = 0L
            amplitudes.clear()

            recordingScope.launch {
                while (isRecording) {
                    delay(100)
                    recordingDurationMs += 100
                    val amplitude = try {
                        if (isRecording && recorder != null) recorder!!.maxAmplitude.toFloat() else 0f
                    } catch (_: Exception) {
                        0f
                    }
                    amplitudes.add(amplitude)
                    if (amplitudes.size > 100) amplitudes.removeAt(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            // Capture final duration instantly for preview
            audioDuration = recordingDurationMs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Cleanup MediaPlayer on dispose or when path changes
    DisposableEffect(voiceRecordingPath) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Playback Timer
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(100)
                playbackPosition = mediaPlayer?.currentPosition?.toLong() ?: 0L
                if (playbackPosition >= audioDuration - 200) {
                    isPlaying = false
                    playbackPosition = audioDuration
                }
            }
        }
    }

    fun togglePlayback() {
        if (voiceRecordingPath == null) return
        
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(voiceRecordingPath)
                prepare()
                audioDuration = duration.toLong()
                setOnCompletionListener {
                    isPlaying = false
                    playbackPosition = duration.toLong()
                }
            }
        }

        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            if (playbackPosition >= audioDuration) {
                mediaPlayer?.seekTo(0)
                playbackPosition = 0
            }
            mediaPlayer?.start()
        }
        isPlaying = mediaPlayer?.isPlaying ?: false
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
            text = {
                TimePicker(state = timePickerState)
            },
            containerColor = Surface
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.9f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Header with Title and Save Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingNote == null) "New Relationship Note" else "Edit Relationship Note",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    TextButton(
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
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Picker
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .then(
                                    if (!isContactLocked) {
                                        Modifier.clickable { showPickerOverlay = !showPickerOverlay }
                                    } else Modifier
                                )
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
                                    Icon(
                                        imageVector = if (showPickerOverlay) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }

                        if (showPickerOverlay && !isContactLocked) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    CompactSearchField(
                                        value = contactSearchQuery,
                                        onValueChange = onContactSearchQueryChange,
                                        placeholder = "Type to filter contacts...",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                                        if (searchedContacts.isEmpty() && contactSearchQuery.isNotEmpty()) {
                                            Text(
                                                text = "No contacts found",
                                                color = TextSecondary,
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            LazyColumn {
                                                items(searchedContacts) { contact ->
                                                    Text(
                                                        text = contact.fullName,
                                                        color = Color.White,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectedContact = contact
                                                                showPickerOverlay = false
                                                                onContactSearchQueryChange("")
                                                            }
                                                            .padding(16.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    StitchTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Note Title",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StitchTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = "Content",
                        singleLine = false,
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compact Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .clickable { showDatePicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = selectedDate.format(dateFormatter), color = Color.White, fontSize = 14.sp)
                        }

                        // Time Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .clickable { showTimePicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = selectedTime.format(timeFormatter), color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Attachment Preview
                    if (attachmentPaths.isNotEmpty()) {
                        Text(
                            text = "Attachments (${attachmentPaths.size})",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            itemsIndexed(attachmentPaths) { index, path ->
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(
                                        model = path,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .clickable { onRemoveAttachment(index) }
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (isRecording) {
                        Spacer(modifier = Modifier.height(16.dp))
                        RecordingIndicator(
                            durationMs = recordingDurationMs,
                            amplitudes = amplitudes
                        )
                    }

                    if (voiceRecordingPath != null && !isRecording) {
                        Spacer(modifier = Modifier.height(16.dp))
                        VoiceMemoPlayerCard(
                            isPlaying = isPlaying,
                            positionMs = playbackPosition,
                            durationMs = audioDuration,
                            onTogglePlayback = { togglePlayback() },
                            onSeek = { 
                                playbackPosition = it
                                mediaPlayer?.seekTo(it.toInt())
                            },
                            onDelete = {
                                mediaPlayer?.release()
                                mediaPlayer = null
                                voiceRecordingPath = null
                                amplitudes.clear()
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(120.dp)) // Buffer for sticky footer
                }
            }

            // Sticky Bottom Toolbar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime),
                color = Surface,
                tonalElevation = 8.dp
            ) {
                Column {
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { attachmentLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Image, null, tint = AccentCyan)
                            }
                            IconButton(onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    val file = File.createTempFile("temp_camera_", ".jpg", context.cacheDir)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, null, tint = AccentCyan)
                            }
                        }
                        
                        // Large Mic Button
                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) Color.Red.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center, 
                                modifier = Modifier.clickable { 
                                    if (isRecording) stopRecording() else startRecording()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, 
                                    contentDescription = null, 
                                    tint = if (isRecording) Color.Red else AccentCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingIndicator(
    durationMs: Long,
    amplitudes: List<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatDuration(durationMs),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            // Simple dynamic amplitude visualization
            Box(modifier = Modifier.weight(1f).height(40.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 4.dp.toPx()
                    val gap = 2.dp.toPx()
                    val maxBars = (width / (barWidth + gap)).toInt()
                    
                    val visibleAmplitudes = amplitudes.takeLast(maxBars)
                    visibleAmplitudes.forEachIndexed { index, amplitude ->
                        val barHeight = (amplitude / 32767f) * height * 2f // Scale factor
                        val x = index * (barWidth + gap)
                        drawRect(
                            color = Color.Red,
                            topLeft = androidx.compose.ui.geometry.Offset(x, (height - barHeight) / 2),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceMemoPlayerCard(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AccentCyan
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
                        onValueChange = { onSeek((it * durationMs).toLong()) },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDuration(positionMs), color = TextSecondary, fontSize = 10.sp)
                        Text(formatDuration(durationMs), color = TextSecondary, fontSize = 10.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
