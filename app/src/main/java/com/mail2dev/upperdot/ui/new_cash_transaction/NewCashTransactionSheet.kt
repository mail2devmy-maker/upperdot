package com.mail2dev.upperdot.ui.new_cash_transaction

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.upperdot.ui.components.CompactSearchField
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.insights.ContactSummary
import com.mail2dev.upperdot.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaRecorder
import java.io.File
import com.mail2dev.upperdot.utils.StorageUtils
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCashTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (Long, Boolean, String, String, String, List<String>, String?) -> Unit,
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
    var selectedContact by remember { mutableStateOf(initialContact) }
    var isRevenue by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    
    // Picker State
    var showPickerOverlay by remember { mutableStateOf(false) }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDateText = remember(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { formatter.format(Date(it)) } ?: formatter.format(Date())
    }

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
    var voiceRecordingPath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }

    fun startRecording() {
        try {
            val folder = File(context.filesDir, "attachments")
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "trans_voice_${System.currentTimeMillis()}.mp4")
            voiceRecordingPath = file.absolutePath
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Cash Transaction",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Overhauled Read-Only Contact Picker with Search Overlay
            Column(modifier = Modifier.fillMaxWidth()) {
                // Primary Read-Only Field (Button-like)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Surface, RoundedCornerShape(16.dp))
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Sleek Compact Internal Search Input
                            CompactSearchField(
                                value = contactSearchQuery,
                                onValueChange = onContactSearchQueryChange,
                                placeholder = "Type to filter contacts...",
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(modifier = Modifier.heightIn(max = 240.dp)) {
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
                                                        onContactSearchQueryChange("") // Reset query
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

            // Date Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = selectedDateText, color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice and Attachments Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Receipts / Attachments (${attachmentPaths.size})",
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
                        Box(modifier = Modifier.size(64.dp)) {
                            AsyncImage(
                                model = path,
                                contentDescription = "Attachment $index",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Delete Button Overlay
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .clickable { onRemoveAttachment(index) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                    
                    item {
                        // Add Button Box
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                                .clickable { attachmentLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Receipt", tint = AccentCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Recording Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isRecording) Color.Red.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier.clickable { 
                            if (isRecording) stopRecording() else startRecording()
                        }
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, 
                            contentDescription = "Record Memo", 
                            tint = if (isRecording) Color.Red else AccentCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    selectedContact?.let { contact ->
                        onSave(contact.id, isRevenue, title, amount, detail, attachmentPaths, voiceRecordingPath)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRevenue) PositiveGreen else AccentCyan,
                    contentColor = Color.Black
                ),
                enabled = selectedContact != null && title.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text(text = "Finalize Transaction", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
