package com.mail2dev.upperdot.ui.profile_settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.components.UpperDotBottomNavigation
import com.mail2dev.upperdot.ui.digital_wallet.DigitalWalletViewModel
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.ui.wallet_overlay.QuickWalletOverlaySheet
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyProfileSettingsScreen(
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileSettingsViewModel,
    walletViewModel: DigitalWalletViewModel
) {
    val userSummary by viewModel.userSummary.collectAsState()
    val cloudMetadata by viewModel.cloudMetadata.collectAsState()
    val bankCards by walletViewModel.bankCards.collectAsState()
    val showQuickWallet by walletViewModel.showQuickWalletSheet.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val rotation = rememberInfiniteTransition(label = "sync_rotation")
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    LaunchedEffect(Unit) {
        viewModel.syncEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showQuickWallet) {
        QuickWalletOverlaySheet(
            onDismiss = walletViewModel::dismissQuickWalletSheet,
            onNavigateToManagement = { onNavigate("digital_wallet_management") },
            onNavigateToPlans = { onNavigate("plans") },
            bankCards = bankCards,
            isPremium = userSummary.isPremium
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            UpperDotBottomNavigation(
                currentRoute = "my_profile",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { walletViewModel.onQuickWalletRequested() },
                containerColor = Color.Black,
                contentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .border(1.dp, AccentCyan, CircleShape)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Quick Wallet")
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "My Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // User Account Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userSummary.name,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userSummary.email,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (userSummary.isPremium) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentCyan.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Premium",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cloud Status Section
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .then(if (isSyncing) Modifier.rotate(angle) else Modifier)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Last Sync: ${userSummary.lastSync}",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                    if (cloudMetadata.exists) {
                                        Text(
                                            text = "☁️ Cloud Backup: ${cloudMetadata.lastModified} (${cloudMetadata.size})",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Button 1: Restore
                                Button(
                                    onClick = viewModel::onRestoreClicked,
                                    enabled = cloudMetadata.exists && !isSyncing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentCyan,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("⬇️ Restore Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Button 2: Backup
                                Button(
                                    onClick = viewModel::onBackupClicked,
                                    enabled = !isSyncing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.DarkGray.copy(alpha = 0.5f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("⬆️ Backup Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Out compact button
                    Surface(
                        onClick = { viewModel.onSignOut(onSignOut) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NegativeRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = NegativeRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                color = NegativeRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metrics Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(icon = Icons.Default.Groups, count = userSummary.contactCount, label = "Contacts")
                        MetricItem(icon = Icons.Default.Description, count = userSummary.noteCount, label = "Notes")
                        MetricItem(icon = Icons.Default.CreditCard, count = userSummary.transactionCount, label = "Trans")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "WORKSPACE CONTROLS DECK",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Menu Deck
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column {
                    MenuListItem(
                        icon = Icons.Default.CreditCard,
                        title = "My Digital Business Wallet",
                        subtitle = "Secure card storage and dynamic keys",
                        onClick = { onNavigate("digital_wallet_management") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                    MenuListItem(
                        icon = Icons.Default.AccountTree,
                        title = "Manage Custom Groups",
                        subtitle = "Configure custom relational categories",
                        onClick = { onNavigate("manage_custom_groups") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                    MenuListItem(
                        icon = Icons.Default.Settings,
                        title = "Advanced App Settings",
                        subtitle = "Storage configuration, local exports & backups",
                        onClick = { onNavigate("advanced_app_settings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                    MenuListItem(
                        icon = Icons.Default.Stars,
                        title = "Free vs Premium Plan",
                        subtitle = "Check tiers limits and security controls",
                        onClick = { onNavigate("plans") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MetricItem(icon: ImageVector, count: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$count $label", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MenuListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(12.dp)
        )
    }
}
