package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.utils.toFormattedDate
import java.io.File
import java.util.Locale
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerSheet(
    note: NoteEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (fullScreenImagePath != null) {
        FullScreenImagePreview(
            path = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Note?", color = Color.White) },
            text = { Text("Are you sure you want to permanently remove this note?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(note)
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Surface
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = { onEdit(note) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AccentCyan
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NegativeRed)
                    }
                }
            }

            Text(
                text = note.createdAt.toFormattedDate(),
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = note.content,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            if (note.voiceRecordingPath != null) {
                AudioPlayerSection(audioPath = note.voiceRecordingPath)
            }

            if (note.attachmentPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Attachments", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(note.attachmentPaths) { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray)
                                .clickable { fullScreenImagePath = path },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionViewerSheet(
    transaction: TransactionEntity,
    currencySymbol: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (fullScreenImagePath != null) {
        FullScreenImagePreview(
            path = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction?", color = Color.White) },
            text = { Text("Are you sure you want to permanently remove this financial log?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete(transaction)
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Surface
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.createdAt.toFormattedDate(),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row {
                    IconButton(onClick = { onEdit(transaction) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AccentCyan
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NegativeRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Details", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                val amountText = if (transaction.isRevenue) {
                    "+$currencySymbol${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
                } else {
                    "-$currencySymbol${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
                }
                val amountColor = if (transaction.isRevenue) PositiveGreen else NegativeRed
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = amountColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = amountText,
                        color = amountColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.detail,
                color = Color.White,
                fontSize = 14.sp
            )

            if (transaction.voiceRecordingPath != null) {
                AudioPlayerSection(audioPath = transaction.voiceRecordingPath)
            }

            if (transaction.receiptPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Receipts", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transaction.receiptPaths) { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray)
                                .clickable { fullScreenImagePath = path },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImagePreview(
    path: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(path),
                contentDescription = "Full Screen Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun AudioPlayerSection(
    audioPath: String
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var audioDuration by remember { mutableLongStateOf(0L) }

    // Initialize duration
    LaunchedEffect(audioPath) {
        audioDuration = getAudioDuration(audioPath)
    }

    // Cleanup MediaPlayer
    DisposableEffect(audioPath) {
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
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
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

    Column {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Voice Memo", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.DarkGray.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { togglePlayback() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = if (audioDuration > 0) playbackPosition.toFloat() / audioDuration else 0f,
                        onValueChange = { 
                            val newPos = (it * audioDuration).toLong()
                            playbackPosition = newPos
                            mediaPlayer?.seekTo(newPos.toInt())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.Gray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDuration(playbackPosition), color = TextSecondary, fontSize = 10.sp)
                        Text(formatDuration(audioDuration), color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

fun getAudioDuration(path: String): Long {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
