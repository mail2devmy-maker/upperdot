package com.mail2dev.upperdot.ui.connections_list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.components.UpperDotBottomNavigation
import com.mail2dev.upperdot.ui.new_cash_transaction.TransactionSheet
import com.mail2dev.upperdot.ui.new_relationship_note.RelationshipNoteSheet
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import com.mail2dev.upperdot.ui.theme.StitchDesignSystem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsListScreen(
    onNavigate: (String) -> Unit,
    onNavigateToContact: (Long) -> Unit,
    onNavigateToAddContact: () -> Unit,
    viewModel: ConnectionsListViewModel = viewModel(),
    initialPhone: String? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
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
    val isMediaCompressionEnabled by viewModel.isMediaCompressionEnabled.collectAsState()

    var whatsappTargetContact by remember { mutableStateOf<ContactSummary?>(null) }
    val context = LocalContext.current

    fun launchWhatsApp(number: String) {
        val cleaned = number.filter { it.isDigit() }
        val normalized = if (cleaned.startsWith("0")) "60$cleaned" else cleaned
        val uri = Uri.parse("https://wa.me/$normalized")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // Fallback to browser
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    if (whatsappTargetContact != null) {
        val contact = whatsappTargetContact!!
        val whatsappProfile = contact.socialProfiles.find { it.platform.equals("WhatsApp", ignoreCase = true) }
        
        if (whatsappProfile != null && whatsappProfile.handle.isNotBlank()) {
            launchWhatsApp(whatsappProfile.handle)
            whatsappTargetContact = null
        } else {
            val numbers = contact.phoneNumbers.filter { it.isNotBlank() }
            when {
                numbers.isEmpty() -> {
                    android.widget.Toast.makeText(context, "No phone number available", android.widget.Toast.LENGTH_SHORT).show()
                    whatsappTargetContact = null
                }
                numbers.size == 1 -> {
                    launchWhatsApp(numbers[0])
                    whatsappTargetContact = null
                }
                else -> {
                    AlertDialog(
                        onDismissRequest = { whatsappTargetContact = null },
                        title = { Text("Select number for WhatsApp", color = Color.White) },
                        text = {
                            Column {
                                numbers.forEach { number ->
                                    TextButton(
                                        onClick = {
                                            launchWhatsApp(number)
                                            whatsappTargetContact = null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(number, color = AccentCyan, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { whatsappTargetContact = null }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        },
                        containerColor = Surface
                    )
                }
            }
        }
    }

    LaunchedEffect(initialPhone) {
        if (!initialPhone.isNullOrEmpty()) {
            viewModel.onAddNoteByPhone(initialPhone)
        }
    }

    if (showAddNoteSheet) {
        RelationshipNoteSheet(
            onDismiss = viewModel::dismissAddNoteSheet,
            onSave = { contactId, title, content, attachments, voice, noteId, createdAt ->
                viewModel.saveNote(contactId, title, content, attachments, voice, noteId, createdAt)
            },
            contactSearchQuery = contactSearchQuery,
            onContactSearchQueryChange = viewModel::onContactSearchQueryChanged,
            searchedContacts = searchedContacts.map { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            attachmentPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            isMediaCompressionEnabled = isMediaCompressionEnabled,
            initialContact = preSelectedContact?.let { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            isContactLocked = preSelectedContact != null
        )
    }

    if (showAddTransactionSheet) {
        TransactionSheet(
            onDismiss = viewModel::dismissAddTransactionSheet,
            onSave = { contactId, isRevenue, title, amount, detail, attachments, voice, transactionId, createdAt ->
                viewModel.saveTransaction(contactId, isRevenue, title, amount, detail, attachments, voice, transactionId, createdAt)
            },
            contactSearchQuery = contactSearchQuery,
            onContactSearchQueryChange = viewModel::onContactSearchQueryChanged,
            searchedContacts = searchedContacts.map { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            receiptPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            isMediaCompressionEnabled = isMediaCompressionEnabled,
            initialContact = preSelectedContact?.let { com.mail2dev.upperdot.ui.insights.ContactSummary(it.id, it.fullName) },
            isContactLocked = preSelectedContact != null
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                UpperDotBottomNavigation(
                    currentRoute = "connections_list",
                    onNavigate = onNavigate
                )
            },
            floatingActionButton = {
                StitchDesignSystem.FAB(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Contact",
                    onClick = onNavigateToAddContact,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Standardized Left-Aligned Top Bar
                StitchDesignSystem.TopBar(
                    title = "Connections",
                    leadingIcon = Icons.Default.Groups
                )

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Search by name or number...", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
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
                            EmptyConnectionsView(
                                searchQuery = searchQuery,
                                onAddContact = {
                                    onNavigateToAddContact()
                                }
                            )
                        }
                        is ConnectionsUIState.Success -> {
                            ConnectionsList(
                                contacts = state.contacts,
                                onContactClick = onNavigateToContact,
                                onAddNote = viewModel::onAddNote,
                                onAddTransaction = viewModel::onAddTransaction,
                                onWhatsAppClick = { contact ->
                                    whatsappTargetContact = contact
                                },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedContentScope = animatedContentScope
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
fun EmptyConnectionsView(
    searchQuery: String = "",
    onAddContact: () -> Unit = {}
) {
    val isSearch = searchQuery.isNotEmpty()
    
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
                            imageVector = if (isSearch) Icons.Default.SearchOff else Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isSearch) "No Contacts Found" else "No All Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isSearch) 
                        "We couldn't find any results matching '$searchQuery'. Try checking for typos or different terms."
                    else 
                        "Your directory is currently empty. Start building your secure network by adding new profile keys.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                if (isSearch) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddContact,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add '$searchQuery'?", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionsList(
    contacts: List<ContactSummary>,
    onContactClick: (Long) -> Unit,
    onAddNote: (Long) -> Unit,
    onAddTransaction: (Long) -> Unit,
    onWhatsAppClick: (ContactSummary) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactCard(
                contact = contact,
                onClick = { onContactClick(contact.id) },
                onAddNote = { onAddNote(contact.id) },
                onAddTransaction = { onAddTransaction(contact.id) },
                onWhatsAppClick = { onWhatsAppClick(contact) },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: ContactSummary,
    onClick: () -> Unit,
    onAddNote: () -> Unit,
    onAddTransaction: () -> Unit,
    onWhatsAppClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    // Track absolute physical spacing with complete velocity bypass
    val offsetX = remember { Animatable(0f) }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val hasPhoneNumber = contact.primaryPhone.isNotEmpty()
        
        // Render Background Controls
        val progress = (offsetX.value / widthPx).coerceIn(0f, 1f)
        val alpha = progress.coerceIn(0f, 1f)
        
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (hasPhoneNumber) Color(0xFF4CAF50).copy(alpha = alpha) 
                    else Color.DarkGray.copy(alpha = alpha * 0.5f)
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = if (hasPhoneNumber) Icons.Default.Call else Icons.Default.PhoneDisabled,
                contentDescription = "Call",
                tint = Color.White.copy(alpha = alpha)
            )
        }

        // Primary Card Layer with Custom Pointer Input Engine
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { isExpanded = !isExpanded },
                        onLongPress = { isExpanded = !isExpanded } // Keep both as requested or just standard tap
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                // Accumulate raw movement delta, strictly clamped left-to-right
                                val newValue = (offsetX.value + dragAmount).coerceIn(0f, widthPx)
                                offsetX.snapTo(newValue)
                            }
                        },
                        onDragEnd = {
                            val percentage = offsetX.value / widthPx
                            scope.launch {
                                if (percentage > 0.65f) {
                                    // STRICT VALIDATION: Paste 65% triggers off-screen animate + execution
                                    offsetX.animateTo(widthPx)
                                    
                                    if (hasPhoneNumber) {
                                        com.mail2dev.upperdot.util.TelephonyUtils.placeOutgoingCall(context, contact.primaryPhone)
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "No number to call for this contact",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    
                                    // Reset back to closed position smoothly
                                    offsetX.animateTo(0f)
                                } else {
                                    // REJECTION: Snap back via soft spring
                                    offsetX.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    )
                }
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val avatarModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
                        with(sharedTransitionScope) {
                            Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = "avatar_${contact.id}"),
                                    animatedVisibilityScope = animatedContentScope
                                )
                        }
                    } else {
                        Modifier
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .size(48.dp)
                            .then(avatarModifier)
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
                    Column(modifier = Modifier.weight(1f)) {
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

                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "View Profile",
                            tint = Color.Gray
                        )
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionButton(
                                painter = painterResource(R.drawable.ic_whatsapp),
                                text = "WhatsApp",
                                onClick = onWhatsAppClick,
                                modifier = Modifier.weight(1f),
                                tint = Color.Unspecified
                            )
                            QuickActionButton(
                                icon = Icons.AutoMirrored.Filled.NoteAdd,
                                text = "Add Note",
                                onClick = onAddNote,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                text = "Add Trans",
                                onClick = onAddTransaction,
                                modifier = Modifier.weight(1f)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painter: Painter? = null,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AccentCyan
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
