package com.mail2dev.upperdot.ui.insights

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.components.FilterCapsule
import com.mail2dev.upperdot.ui.components.NoteViewerSheet
import com.mail2dev.upperdot.ui.components.TransactionViewerSheet
import com.mail2dev.upperdot.ui.components.UpperDotBottomNavigation
import com.mail2dev.upperdot.ui.new_cash_transaction.NewCashTransactionSheet
import com.mail2dev.upperdot.ui.new_relationship_note.NewRelationshipNoteSheet
import com.mail2dev.upperdot.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigate: (String) -> Unit,
    viewModel: InsightsViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedContactFilter by viewModel.selectedContactFilter.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val showAddNoteSheet by viewModel.showAddNoteSheet.collectAsState()
    val showAddTransactionSheet by viewModel.showAddTransactionSheet.collectAsState()
    val contactNames by viewModel.contactNames.collectAsState()
    val selectedAttachments by viewModel.selectedAttachments.collectAsState()
    val contactSearchQuery by viewModel.contactSearchQuery.collectAsState()
    val searchedContacts by viewModel.searchedContacts.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val selectedNote by viewModel.selectedNote.collectAsState()
    val selectedTransaction by viewModel.selectedTransaction.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showAddNoteSheet) {
        NewRelationshipNoteSheet(
            onDismiss = {
                viewModel.dismissAddNoteSheet()
                viewModel.clearTemporaryNoteState()
            },
            onSave = { contactId, title, content, attachments, voice -> 
                viewModel.saveNote(contactId, title, content, attachments, voice) 
                viewModel.clearTemporaryNoteState()
            },
            contactSearchQuery = contactSearchQuery,
            onContactSearchQueryChange = viewModel::onContactSearchQueryChanged,
            searchedContacts = searchedContacts,
            attachmentPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            isContactLocked = false
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
            searchedContacts = searchedContacts,
            attachmentPaths = selectedAttachments,
            onAddAttachment = viewModel::addAttachmentPath,
            onRemoveAttachment = viewModel::removeAttachmentPath,
            currencySymbol = currencySymbol,
            isContactLocked = false
        )
    }

    if (selectedNote != null) {
        NoteViewerSheet(
            note = selectedNote!!,
            sheetState = sheetState,
            onDismiss = viewModel::dismissNoteViewer
        )
    }

    if (selectedTransaction != null) {
        TransactionViewerSheet(
            transaction = selectedTransaction!!,
            currencySymbol = currencySymbol,
            sheetState = sheetState,
            onDismiss = viewModel::dismissTransactionViewer
        )
    }

    Scaffold(
        bottomBar = {
            UpperDotBottomNavigation(
                currentRoute = "insights",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == InsightTab.NOTES) viewModel.onAddNoteClicked()
                    else viewModel.onAddTransactionClicked()
                },
                containerColor = Color.Black,
                contentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .border(1.dp, AccentCyan, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color.Black
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
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Insights",
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
                placeholder = {
                    Text(
                        if (selectedTab == InsightTab.NOTES) "Search notes..." else "Search transactions...",
                        color = TextSecondary
                    )
                },
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

            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedContactFilter == null) {
                    FilterCapsule(
                        text = "All Contacts",
                        isSelected = false,
                        onClick = { /* Select Contact Dialog */ }
                    )
                } else {
                    FilterCapsule(
                        text = "Clear Filters X",
                        isSelected = false,
                        backgroundColor = Color(0xFF3B1F1F), // Dark red background
                        contentColor = NegativeRed,
                        onClick = { viewModel.clearFilters() }
                    )
                    FilterCapsule(
                        text = selectedContactFilter!!,
                        isSelected = true,
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Grid (Visible only on Transactions Tab)
            AnimatedVisibility(visible = selectedTab == InsightTab.TRANSACTIONS) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "Revenue",
                            value = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", totalRevenue)}",
                            valueColor = PositiveGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Expenses",
                            value = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", totalExpenses)}",
                            valueColor = NegativeRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Net Profit",
                            value = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", netProfit)}",
                            valueColor = AccentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Surface, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                TabItem(
                    title = "NOTES",
                    isSelected = selectedTab == InsightTab.NOTES,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTabSelected(InsightTab.NOTES) }
                )
                TabItem(
                    title = "TRANSACTIONS",
                    isSelected = selectedTab == InsightTab.TRANSACTIONS,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTabSelected(InsightTab.TRANSACTIONS) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            if (selectedTab == InsightTab.NOTES) {
                NotesList(notes = notes, onContactClick = viewModel::onContactFilterSelected, onNoteClick = viewModel::selectNote)
            } else {
                TransactionsList(
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    onContactClick = viewModel::onContactFilterSelected,
                    onTransactionClick = viewModel::selectTransaction
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) AccentCyan else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NotesList(
    notes: List<NoteEntry>,
    onContactClick: (String) -> Unit,
    onNoteClick: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note = note, onContactClick = { onContactClick(note.contactName) }, onClick = { onNoteClick(note.id) })
        }
    }
}

@Composable
fun TransactionsList(
    transactions: List<TransactionEntry>,
    currencySymbol: String,
    onContactClick: (String) -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionCard(
                transaction = transaction,
                currencySymbol = currencySymbol,
                onContactClick = { onContactClick(transaction.contactName) },
                onClick = { onTransactionClick(transaction.id) }
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntry,
    currencySymbol: String,
    onContactClick: () -> Unit,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = transaction.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onContactClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = transaction.contactName, color = AccentCyan, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.detail,
                    color = if (isExpanded) Color.White else TextSecondary,
                    fontSize = 14.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    modifier = Modifier.weight(1f)
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (transaction.isRevenue) "+$currencySymbol${transaction.amount}" else "-$currencySymbol${transaction.amount}",
                        color = if (transaction.isRevenue) PositiveGreen else NegativeRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (transaction.isRevenue) "REVENUE" else "EXPENSE",
                        color = if (transaction.isRevenue) PositiveGreen.copy(alpha = 0.6f) else NegativeRed.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = transaction.timestamp, color = TextMuted, fontSize = 10.sp)

                if (transaction.attachmentCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Text(text = "${transaction.attachmentCount}", color = AccentCyan, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntry,
    onContactClick: () -> Unit,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = note.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onContactClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = note.contactName, color = AccentCyan, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = note.content,
                color = if (isExpanded) Color.White else TextSecondary,
                fontSize = 14.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = note.timestamp, color = TextMuted, fontSize = 10.sp)
                
                if (note.attachmentCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Text(text = "${note.attachmentCount}", color = AccentCyan, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
