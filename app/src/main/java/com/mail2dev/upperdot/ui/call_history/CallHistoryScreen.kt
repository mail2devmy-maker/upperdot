package com.mail2dev.upperdot.ui.call_history

import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.mail2dev.upperdot.ui.components.UpperDotBottomNavigation
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.utils.toFormattedDate

@Composable
fun CallHistoryScreen(
    onNavigate: (String) -> Unit,
    onNavigateToDialer: () -> Unit,
    onNavigateToContact: (Long) -> Unit,
    onNavigateToAddContact: (String) -> Unit,
    viewModel: CallHistoryViewModel
) {
    val hasPermission by viewModel.hasPermission.collectAsState()
    val groupedLogs by viewModel.groupedCallLogs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.updatePermissionState(granted)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Scaffold(
            bottomBar = {
                UpperDotBottomNavigation(
                    currentRoute = "call_history",
                    onNavigate = onNavigate
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToDialer,
                    containerColor = AccentCyan,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Dialpad, contentDescription = "Open Dialpad")
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Call History",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!hasPermission) {
                    EmptyCallHistoryCard()
                } else if (groupedLogs.isEmpty()) {
                    if (isRefreshing) {
                        // Background loading for the first time
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentCyan)
                        }
                    } else {
                        EmptyCallHistoryCard()
                    }
                } else {
                    CallLogList(
                        groupedLogs = groupedLogs,
                        onContactClick = onNavigateToContact,
                        onAddContactClick = onNavigateToAddContact,
                        onToggleExpand = viewModel::toggleExpand,
                        onCallClick = { number ->
                            try {
                                val intent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:$number")
                                }
                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CALL_PHONE
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    context.startActivity(intent)
                                } else {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$number")
                                    }
                                    context.startActivity(dialIntent)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("CallDebug", "Error placing call", e)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCallHistoryCard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Cellular Records",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ensure Call Log permissions are enabled in your device settings to sync telephony data.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CallLogList(
    groupedLogs: List<GroupedCallLog>,
    onContactClick: (Long) -> Unit,
    onAddContactClick: (String) -> Unit,
    onToggleExpand: (GroupedCallLog) -> Unit,
    onCallClick: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(groupedLogs) { group ->
            GroupedCallCard(
                group = group,
                onToggleExpand = { onToggleExpand(group) },
                onAddClick = { onAddContactClick(group.number) },
                onContactClick = { group.contactId?.let { onContactClick(it) } },
                onCallClick = onCallClick
            )
        }
    }
}

@Composable
fun GroupedCallCard(
    group: GroupedCallLog,
    onToggleExpand: () -> Unit,
    onAddClick: () -> Unit,
    onContactClick: () -> Unit,
    onCallClick: (String) -> Unit
) {
    val lastCall = group.calls.first()
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onToggleExpand() }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = when (lastCall.type) {
                    CallLog.Calls.INCOMING_TYPE -> Icons.AutoMirrored.Filled.CallReceived
                    CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
                    CallLog.Calls.MISSED_TYPE -> Icons.AutoMirrored.Filled.CallMissed
                    else -> Icons.Default.Call
                }
                val iconTint = if (lastCall.type == CallLog.Calls.MISSED_TYPE) NegativeRed else AccentCyan

                Box(modifier = Modifier.size(40.dp)) {
                    if (group.avatarPath != null) {
                        AsyncImage(
                            model = group.avatarPath,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay call type icon
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.name ?: group.number,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (group.calls.size > 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = AccentCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = group.calls.size.toString(),
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = if (group.name != null) group.number else "Unknown Number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = lastCall.timestamp.toFormattedDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )
                }

                FilledTonalIconButton(
                    onClick = { onCallClick(group.number) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AccentCyan.copy(alpha = 0.15f),
                        contentColor = AccentCyan
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (group.isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (group.contactId == null) {
                        TextButton(
                            onClick = onAddClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Contact", fontSize = 11.sp, color = AccentCyan)
                        }
                    } else {
                        TextButton(
                            onClick = onContactClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("View Profile", fontSize = 11.sp, color = AccentCyan)
                        }
                    }
                }
                
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                
                group.calls.forEach { call ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subIcon = when (call.type) {
                            CallLog.Calls.INCOMING_TYPE -> Icons.AutoMirrored.Filled.CallReceived
                            CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
                            CallLog.Calls.MISSED_TYPE -> Icons.AutoMirrored.Filled.CallMissed
                            else -> Icons.Default.Call
                        }
                        Icon(
                            subIcon,
                            contentDescription = null,
                            tint = if (call.type == CallLog.Calls.MISSED_TYPE) NegativeRed else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = call.timestamp.toFormattedDate(),
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = when (call.type) {
                                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                                CallLog.Calls.MISSED_TYPE -> "Missed"
                                else -> "Call"
                            },
                            color = Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
