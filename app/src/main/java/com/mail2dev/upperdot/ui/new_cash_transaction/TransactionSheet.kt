package com.mail2dev.upperdot.ui.new_cash_transaction

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.ui.components.CompactSearchField
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.insights.ContactSummary
import com.mail2dev.upperdot.ui.new_relationship_note.RecordingIndicator
import com.mail2dev.upperdot.ui.new_relationship_note.VoiceMemoPlayerCard
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.NegativeRed
import com.mail2dev.upperdot.ui.theme.PositiveGreen
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
fun TransactionSheet(
    onDismiss: () -> Unit,
    onSave: (Long, Boolean, String, String, String, List<String>, String?, Long?, Long) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null,
    existingTransaction: TransactionEntity? = null,
    contactSearchQuery: String,
    onContactSearchQueryChange: (String) -> Unit,
    searchedContacts: List<ContactSummary>,
    receiptPaths: List<String>,
    onAddAttachment: (String) -> Unit,
    onRemoveAttachment: (Int) -> Unit,
    currencySymbol: String,
    initialContact: ContactSummary? = null,
    isContactLocked: Boolean = false
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val internalPath = StorageUtils.copyUriToInternalStorage(context, tempCameraUri!!)
            if (internalPath != null) {
                onAddAttachment(internalPath)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(
                context.cacheDir,
                "receipt_${System.currentTimeMillis()}.jpg"
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    var selectedContact by remember(initialContact) { mutableStateOf(initialContact) }
    var isRevenue by remember(existingTransaction) { mutableStateOf(existingTransaction?.isRevenue ?: true) }
    var title by remember(existingTransaction) { mutableStateOf(existingTransaction?.title ?: "") }
    var amount by remember(existingTransaction) { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var detail by remember(existingTransaction) { mutableStateOf(existingTransaction?.detail ?: "") }
    
    // Picker State
    var showPickerOverlay by remember { mutableStateOf(false) }

    // Date and Time State
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val initialDateTime = remember(existingTransaction) {
        existingTransaction?.createdAt?.let { 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        } ?: LocalDateTime.now()
    }
    
    var selectedDate by remember(existingTransaction) { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember(existingTransaction) { mutableStateOf(initialDateTime.toLocalTime()) }

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

    // Voice Recording State
    var voiceRecordingPath by remember(existingTransaction) { mutableStateOf<String?>(existingTransaction?.voiceRecordingPath) }
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
            val file = File(folder, "trans_voice_${System.currentTimeMillis()}.mp4")
            
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (existingTransaction != null) {
                            IconButton(onClick = { onDelete?.invoke(existingTransaction) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.7f))
                            }
                        }
                        Text(
                            text = if (existingTransaction == null) "New Cash Transaction" else "Edit Cash Transaction",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    TextButton(
                        onClick = { 
                            selectedContact?.let { contact ->
                                val combinedTimestamp = LocalDateTime.of(selectedDate, selectedTime)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                onSave(contact.id, isRevenue, title, amount, detail, receiptPaths, voiceRecordingPath, existingTransaction?.id, combinedTimestamp)
                            }
                        },
                        enabled = selectedContact != null && title.isNotEmpty() && amount.isNotEmpty() && !isRecording,
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

                    // Transaction Type Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        TransactionTypeItem(
                            title = "REVENUE",
                            isSelected = isRevenue,
                            activeColor = PositiveGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { isRevenue = true }
                        )
                        TransactionTypeItem(
                            title = "EXPENSE",
                            isSelected = !isRevenue,
                            activeColor = NegativeRed,
                            modifier = Modifier.weight(1f),
                            onClick = { isRevenue = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and Amount Split Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StitchTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Title",
                            modifier = Modifier.weight(0.6f)
                        )
                        StitchTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = "Amount ($currencySymbol)",
                            modifier = Modifier.weight(0.4f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    StitchTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        placeholder = "Detail / Notes",
                        singleLine = false,
                        minLines = 3,
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
                    if (receiptPaths.isNotEmpty()) {
                        Text(
                            text = "Receipts / Attachments (${receiptPaths.size})",
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
                            itemsIndexed(receiptPaths) { index, path ->
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(
                                        model = path,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
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
                            IconButton(onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo", tint = TextSecondary)
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
fun TransactionTypeItem(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
