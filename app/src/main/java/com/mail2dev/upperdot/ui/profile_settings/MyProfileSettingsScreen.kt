package com.mail2dev.upperdot.ui.profile_settings

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextOverflow
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
    val bankCards by walletViewModel.bankCards.collectAsState()
    val showQuickWallet by walletViewModel.showQuickWalletSheet.collectAsState()

    if (showQuickWallet) {
        QuickWalletOverlaySheet(
            onDismiss = walletViewModel::dismissQuickWalletSheet,
            onNavigateToManagement = { onNavigate("digital_wallet_management") },
            onNavigateToPlans = { onNavigate("plans") },
            bankCards = bankCards,
            isPremium = userSummary.isPremium
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Scaffold(
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
            containerColor = Color.Transparent
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

                // New User Account Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column {
                        // Top Section: Identity
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(1.dp, AccentCyan.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userSummary.name,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (userSummary.isPremium) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = AccentCyan.copy(alpha = 0.1f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "PREMIUM",
                                                color = AccentCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = userSummary.email,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Middle Section: Stats Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(count = userSummary.contactCount.toString(), label = "Contacts", modifier = Modifier.weight(1f))
                            MetricCard(count = userSummary.noteCount.toString(), label = "Notes", modifier = Modifier.weight(1f))
                            MetricCard(count = userSummary.transactionCount.toString(), label = "Trans", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bottom Section: Cloud Backup & Sync Footer
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val syncIcon = if (userSummary.isSyncing) Icons.Default.Sync else Icons.Default.CloudDone
                                val rotation by rememberInfiniteTransition(label = "").animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = ""
                                )

                                Icon(
                                    imageVector = syncIcon,
                                    contentDescription = null,
                                    tint = if (userSummary.isSyncing) AccentCyan else Color.Gray,
                                    modifier = Modifier.size(16.dp).then(if (userSummary.isSyncing) Modifier.rotate(rotation) else Modifier)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (userSummary.isSyncing) "Syncing..." else "Last sync: ${userSummary.lastSync}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            if (userSummary.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AccentCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                TextButton(
                                    onClick = { viewModel.triggerManualSync() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)
                                ) {
                                    Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
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
                            icon = Icons.Default.Dns,
                            title = "Data & Cloud Vault Management",
                            subtitle = "Cloud sync, backups and import/export",
                            onClick = { onNavigate("data_vault_hub") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                        MenuListItem(
                            icon = Icons.Default.CreditCard,
                            title = "My Digital Wallet",
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
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                        // Relocated Sign Out
                        MenuListItem(
                            icon = Icons.Default.Logout,
                            iconColor = NegativeRed,
                            title = "Sign Out",
                            subtitle = "Securely end your current session",
                            onClick = { viewModel.onSignOut(onSignOut) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MetricCard(count: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = count, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun MenuListItem(
    icon: ImageVector,
    iconColor: Color = AccentCyan,
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
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
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
