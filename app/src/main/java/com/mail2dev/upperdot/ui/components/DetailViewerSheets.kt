package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerSheet(
    note: NoteEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }

    if (fullScreenImagePath != null) {
        FullScreenImagePreview(
            path = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
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
            Text(
                text = note.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = note.createdAt.toFormattedDate(),
                color = TextSecondary,
                fontSize = 12.sp
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
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(Color.Gray, CircleShape)
                        )
                    }
                }
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
    onDismiss: () -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }

    if (fullScreenImagePath != null) {
        FullScreenImagePreview(
            path = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
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
                verticalAlignment = Alignment.Top
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
                        fontSize = 12.sp
                    )
                }
                
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

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Details", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.detail,
                color = Color.White,
                fontSize = 14.sp
            )

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
