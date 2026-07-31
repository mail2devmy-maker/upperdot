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
    onDismiss: () -> Unit,
    onUpdate: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var isEditingMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    var editedTitle by remember(note) { mutableStateOf(note.title) }
    var editedContent by remember(note) { mutableStateOf(note.content) }

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
                    if (isEditingMode) {
                        StitchTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            placeholder = "Note Title"
                        )
                    } else {
                        Text(
                            text = note.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { 
                        if (isEditingMode) {
                            onUpdate(note.copy(title = editedTitle, content = editedContent))
                            isEditingMode = false
                        } else {
                            isEditingMode = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditingMode) Icons.Default.Save else Icons.Default.Edit,
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
            
            if (isEditingMode) {
                StitchTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    placeholder = "Content",
                    singleLine = false,
                    minLines = 4
                )
            } else {
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
            }

            if (isEditingMode) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onUpdate(note.copy(title = editedTitle, content = editedContent))
                        isEditingMode = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionViewerSheet(
    transaction: TransactionEntity,
    currencySymbol: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onUpdate: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var isEditingMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    var editedTitle by remember(transaction) { mutableStateOf(transaction.title) }
    var editedAmount by remember(transaction) { mutableStateOf(transaction.amount.toString()) }
    var editedDetail by remember(transaction) { mutableStateOf(transaction.detail) }

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
                    if (isEditingMode) {
                        StitchTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            placeholder = "Transaction Title"
                        )
                    } else {
                        Text(
                            text = transaction.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = transaction.createdAt.toFormattedDate(),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row {
                    IconButton(onClick = { 
                        if (isEditingMode) {
                            val newAmount = editedAmount.toDoubleOrNull() ?: transaction.amount
                            onUpdate(transaction.copy(title = editedTitle, amount = newAmount, detail = editedDetail))
                            isEditingMode = false
                        } else {
                            isEditingMode = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditingMode) Icons.Default.Save else Icons.Default.Edit,
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
            
            if (isEditingMode) {
                StitchTextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it },
                    placeholder = "Amount ($currencySymbol)",
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                StitchTextField(
                    value = editedDetail,
                    onValueChange = { editedDetail = it },
                    placeholder = "Details",
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val newAmount = editedAmount.toDoubleOrNull() ?: transaction.amount
                        onUpdate(transaction.copy(title = editedTitle, amount = newAmount, detail = editedDetail))
                        isEditingMode = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
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
            }

            if (!isEditingMode && transaction.receiptPaths.isNotEmpty()) {
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
