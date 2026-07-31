package com.mail2dev.upperdot.ui.connections_list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.components.UpperDotBottomNavigation
import com.mail2dev.upperdot.ui.new_cash_transaction.NewCashTransactionSheet
import com.mail2dev.upperdot.ui.new_relationship_note.NewRelationshipNoteSheet
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.PrimaryYellow
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary

@Composable
fun ConnectionsListScreen(
    onNavigate: (String) -> Unit,
    onNavigateToContact: (Long) -> Unit,
    onNavigateToAddContact: () -> Unit,
    viewModel: ConnectionsListViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val showAddNoteSheet by viewModel.showAddNoteSheet.collectAsState()
    val showAddTransactionSheet by viewModel.showAddTransactionSheet.collectAsState()
    val preSelectedContact by viewModel.preSelectedContact.collectAsState()
    val contactSearchQuery by viewModel.contactSearchQuery.collectAsState()
    val searchedContacts by viewModel.searchedContacts.collectAsState()
    val selectedAttachments by viewModel.selectedAttachments.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    if (showAddNoteSheet) {
        NewRelationshipNoteSheet(
            onDismiss = viewModel::dismissAddNoteSheet,
            onSave = { contactId, title, content, attachments, voice ->
                viewModel.saveNote(contactId, title, content, attachments, voice)
            },
            contactSearchQuery = contactSearchQuery,
            onContactSearchQueryChange = viewModel::onContactSearchQueryChanged,
            searchedContacts = searchedContacts.map { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            attachmentPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            initialContact = preSelectedContact?.let { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            isContactLocked = preSelectedContact != null
        )
    }

    if (showAddTransactionSheet) {
        NewCashTransactionSheet(
            onDismiss = viewModel::dismissAddTransactionSheet,
            onSave = { contactId, isRevenue, title, amount, detail, attachments, voice ->
                viewModel.saveTransaction(contactId, isRevenue, title, amount, detail, attachments, voice)
            },
            contactSearchQuery = contactSearchQuery,
            onContactSearchQueryChange = viewModel::onContactSearchQueryChanged,
            searchedContacts = searchedContacts.map { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            receiptPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            initialContact = preSelectedContact?.let { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            isContactLocked = preSelectedContact != null
        )
    }

    Scaffold(
        bottomBar = {
            UpperDotBottomNavigation(
                currentRoute = "connections_list",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddContact,
                containerColor = AccentCyan,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connections",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Search by name or number...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    disabledContainerColor = Surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AccentCyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filters
            val filters = listOf("All", "Favorites", "Work", "Family", "Vendor", "Unassigned")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    FilterCapsule(
                        text = filter,
                        isSelected = filter == selectedFilter,
                        onClick = { viewModel.onFilterSelected(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            when (val state = uiState) {
                is ConnectionsUIState.Empty -> {
                    EmptyConnectionsView()
                }
                is ConnectionsUIState.Success -> {
                    ConnectionsList(
                        contacts = state.contacts,
                        onContactClick = onNavigateToContact,
                        onDialClick = viewModel::onDialContact,
                        onAddNote = viewModel::onAddNote,
                        onAddTransaction = viewModel::onAddTransaction
                    )
                }
                is ConnectionsUIState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
            }
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
        color = if (isSelected) AccentCyan else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun EmptyConnectionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No All Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your directory is currently empty. Start building your secure network by adding new profile keys.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ConnectionsList(
    contacts: List<ContactSummary>,
    onContactClick: (Long) -> Unit,
    onDialClick: (ContactSummary) -> Unit,
    onAddNote: (Long) -> Unit,
    onAddTransaction: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactCard(
                contact = contact,
                onClick = { onContactClick(contact.id) },
                onDial = { onDialClick(contact) },
                onAddNote = { onAddNote(contact.id) },
                onAddTransaction = { onAddTransaction(contact.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    contact: ContactSummary,
    onClick: () -> Unit,
    onDial: () -> Unit,
    onAddNote: () -> Unit,
    onAddTransaction: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                // EXECUTION LOCK: Fires strictly on release Past 60% threshold
                if (contact.primaryPhone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${contact.primaryPhone}")
                    }
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        context.startActivity(intent)
                    } else {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.primaryPhone}")
                        }
                        context.startActivity(dialIntent)
                    }
                }
                false // Reset to settled with spring animation
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.6f } // Strict 60% Boundary
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Visual Opacity Fading based on swipe progress
            val progress = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                dismissState.progress
            } else 0f
            
            val alpha = progress.coerceIn(0f, 1f)
            val backgroundColor = Color(0xFF4CAF50).copy(alpha = alpha)
            val iconColor = Color.White.copy(alpha = alpha)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = iconColor
                    )
                }
            }
        },
        enableDismissFromEndToStart = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { isExpanded = !isExpanded },
                        onTap = { onClick() }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.fullName.take(1).uppercase(),
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = contact.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (contact.nicknames.isNotEmpty()) {
                            Text(
                                text = contact.nicknames.joinToString(", "),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.NoteAdd,
                                text = "Add Note",
                                onClick = onAddNote
                            )
                            QuickActionButton(
                                icon = Icons.Default.ReceiptLong,
                                text = "Add Trans",
                                onClick = onAddTransaction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
