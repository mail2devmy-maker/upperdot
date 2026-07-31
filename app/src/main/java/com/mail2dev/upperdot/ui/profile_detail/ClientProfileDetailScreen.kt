package com.mail2dev.upperdot.ui.profile_detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.data.local.entity.TransactionEntity
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.utils.toFormattedDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileDetailScreen(
    contactId: Long,
    onNavigateBack: () -> Unit,
    onEditContact: (Long) -> Unit,
    viewModel: ClientProfileDetailViewModel
) {
    val contact by viewModel.contactProfile.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isNotesExpanded by viewModel.isNotesExpanded.collectAsState()
    val isTransactionsExpanded by viewModel.isTransactionsExpanded.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<NoteEntity?>(null) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete this contact? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteContact { 
                        showDeleteDialog = false
                        onNavigateBack() 
                    }
                }) {
                    Text("Delete", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Surface,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (selectedNote != null) {
        NoteViewerSheet(
            note = selectedNote!!,
            sheetState = sheetState,
            onDismiss = { selectedNote = null }
        )
    }

    if (selectedTransaction != null) {
        TransactionViewerSheet(
            transaction = selectedTransaction!!,
            sheetState = sheetState,
            onDismiss = { selectedTransaction = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NegativeRed)
                    }
                    IconButton(onClick = { onEditContact(contactId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        contact?.let { profile ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Profile Avatar
                    Surface(
                        shape = CircleShape,
                        color = Color.Black,
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, AccentCyan, CircleShape)
                            .padding(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.fullName.take(1).uppercase(),
                                color = AccentCyan,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = profile.fullName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (profile.nicknames.isNotEmpty()) {
                        Text(
                            text = "(${profile.nicknames})",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Relationship Group Labels
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Assign Relationship Group:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (profile.group.isNotEmpty()) {
                                FilterCapsule(text = profile.group, isSelected = true) {}
                            }
                            if (profile.tag.isNotEmpty()) {
                                FilterCapsule(text = profile.tag, isSelected = false) {}
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // CONTACT INFO
                item {
                    ProfileCard(title = "CONTACT INFO") {
                        profile.phoneNumbers.forEach { number ->
                            ProfileDataRow(icon = Icons.Default.Phone, label = "PHONE", value = number)
                        }
                        profile.emails.forEach { email ->
                            ProfileDataRow(icon = Icons.Default.Email, label = "EMAIL", value = email)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // BUSINESS INFO
                item {
                    ProfileCard(title = "BUSINESS INFO") {
                        ProfileDataRow(icon = Icons.Default.Business, label = profile.businessCategory, value = profile.companyName)
                        ProfileDataRow(icon = Icons.Default.LocationOn, label = "ADDRESS", value = profile.officeAddress)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // BANK INFO
                item {
                    ProfileCard(title = "BANK INFO") {
                        profile.bankAccounts.forEach { account ->
                            ProfileDataRow(icon = Icons.Default.AccountBalance, label = account.bankName, value = account.accountNumber)
                            ProfileDataRow(icon = Icons.Default.Person, label = "ACCOUNT NAME", value = account.holderName)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SOCIAL PROFILES
                item {
                    ProfileCard(title = "SOCIAL PROFILES") {
                        profile.socialProfiles.forEach { social ->
                            ProfileDataRow(icon = Icons.Default.Circle, label = social.platform, value = social.handle)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // RELATIONSHIP NOTES
                item {
                    CollapsibleSection(
                        title = "RELATIONSHIP NOTES (${notes.size})",
                        isExpanded = isNotesExpanded,
                        onToggle = viewModel::toggleNotes
                    ) {
                        if (notes.isEmpty()) {
                            EmptyStatePlaceholder("No relationship notes recorded yet.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                notes.forEach { note ->
                                    NoteRow(note = note, onClick = { selectedNote = note })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // TRANSACTION LEDGER
                item {
                    CollapsibleSection(
                        title = "TRANSACTION LEDGER (${transactions.size})",
                        isExpanded = isTransactionsExpanded,
                        onToggle = viewModel::toggleTransactions
                    ) {
                        if (transactions.isEmpty()) {
                            EmptyStatePlaceholder("No financial transactions recorded yet.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                transactions.forEach { transaction ->
                                    TransactionRow(transaction = transaction, onClick = { selectedTransaction = transaction })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun NoteRow(note: NoteEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.createdAt.toFormattedDate(),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.voiceRecordingPath != null) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (note.attachmentPaths.isNotEmpty()) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: TransactionEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.createdAt.toFormattedDate(),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            val amountText = if (transaction.isRevenue) {
                "+$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            } else {
                "-$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            }
            val amountColor = if (transaction.isRevenue) PositiveGreen else NegativeRed

            Text(
                text = amountText,
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerSheet(
    note: NoteEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
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
                // Voice playback placeholder
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
                // Attachment carousel placeholder
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    note.attachmentPaths.forEach { _ ->
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.DarkGray
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(16.dp))
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
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
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
                    "+$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
                } else {
                    "-$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
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

            if (transaction.attachmentPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Receipt", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.DarkGray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ProfileDataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.DarkGray.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Square,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            content()
        }
    }
}

@Composable
fun FilterCapsule(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AccentCyan else Surface,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}
