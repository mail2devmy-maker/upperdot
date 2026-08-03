package com.mail2dev.upperdot.ui.app_settings

import android.app.role.RoleManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdvancedSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val syncOverWifi by viewModel.syncOverWifi.collectAsState()
    val syncFrequency by viewModel.syncFrequency.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val showClearCacheDialog by viewModel.showClearCacheDialog.collectAsState()
    val showFrequencyDialog by viewModel.showFrequencyDialog.collectAsState()
    val showCurrencyDialog by viewModel.showCurrencyDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.updateStorageDiagnostics(
            context.filesDir,
            context.getDatabasePath("upperdot.db")
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.Success -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SettingsUiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                SettingsUiEvent.Loading -> {
                    // Handled visually or via specific state if needed
                }
            }
        }
    }

    if (showFrequencyDialog) {
        val options = listOf("1h", "6h", "12h", "24h", "Manual")
        AlertDialog(
            onDismissRequest = { viewModel.dismissFrequencyDialog() },
            title = { Text("Sync Frequency", color = Color.White) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSyncFrequencySelected(option) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == syncFrequency,
                                onClick = { viewModel.onSyncFrequencySelected(option) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                            )
                            Text(text = option, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Surface
        )
    }

    if (showCurrencyDialog) {
        val options = listOf("$", "RM", "€", "£", "¥")
        AlertDialog(
            onDismissRequest = { viewModel.dismissCurrencyDialog() },
            title = { Text("Currency Selection", color = Color.White) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onCurrencySelected(option) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == currencySymbol,
                                onClick = { viewModel.onCurrencySelected(option) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                            )
                            Text(text = option, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Surface
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearCacheDialog() },
            title = { Text("Clear Local Cache") },
            text = { Text("This will only delete temporary image thumbnails. No primary data or offline attachments will be affected. Proceed?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmClearCache(context.filesDir, context.getDatabasePath("upperdot.db")) }) {
                    Text("Clear", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearCacheDialog() }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Surface,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced App Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Surface,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // System Permissions & Roles
            item {
                Text(
                    text = "SYSTEM PERMISSIONS & ROLES",
                    color = Color(0xFF9575CD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                val roleLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { _ -> }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    SettingsListItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Default Phone App",
                        subtitle = "Set UpperDot as your primary dialer",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val roleManager = context.getSystemService(RoleManager::class.java)
                                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                                    if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                                        roleLauncher.launch(intent)
                                    } else {
                                        android.widget.Toast.makeText(context, "UpperDot is already your default dialer", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val telecomManager = context.getSystemService(android.content.Context.TELECOM_SERVICE) as android.telecom.TelecomManager
                                if (telecomManager.defaultDialerPackage != context.packageName) {
                                    val intent = android.content.Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                        putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    android.widget.Toast.makeText(context, "UpperDot is already your default dialer", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            // Storage & Data Management
            item {
                Text(
                    text = "STORAGE & DATA MANAGEMENT",
                    color = Color(0xFF9575CD), // Purple as per SRS
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column {
                        SettingsListItem(
                            icon = Icons.Default.DeleteSweep,
                            iconColor = NegativeRed,
                            title = "Clear Local Cache",
                            subtitle = "Currently using 0.0 MB",
                            onClick = { viewModel.requestClearCache() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                        SettingsListItem(
                            icon = Icons.Default.HighQuality,
                            title = "Media Compression",
                            subtitle = "Medium",
                            onClick = {} // Locked to Medium
                        )
                    }
                }
            }

            // Sync & Connectivity
            item {
                Text(
                    text = "SYNC & CONNECTIVITY",
                    color = Color(0xFF9575CD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column {
                        SettingsToggleItem(
                            icon = Icons.Default.Wifi,
                            title = "Sync Over Wi-Fi Only",
                            subtitle = "Restrict background data transfers",
                            checked = syncOverWifi,
                            onCheckedChange = { viewModel.toggleSyncOverWifi(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                        SettingsValueItem(
                            icon = Icons.Default.Sync,
                            title = "Sync Frequency",
                            value = syncFrequency,
                            onClick = { viewModel.requestFrequencyChange() }
                        )
                    }
                }
            }

            // Localization
            item {
                Text(
                    text = "LOCALIZATION & PREFERENCES",
                    color = Color(0xFF9575CD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    SettingsValueItem(
                        icon = Icons.Default.Language,
                        title = "Currency Selection",
                        value = currencySymbol,
                        onClick = { viewModel.requestCurrencyChange() }
                    )
                }
            }

            // Diagnostics
            item {
                Text(
                    text = "DATABASE DIAGNOSTICS INFO",
                    color = Color(0xFF9575CD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DiagnosticRow(label = "Vault File Size (DB)", value = diagnostics.vaultSize)
                    DiagnosticRow(label = "Total Attachment Usage", value = diagnostics.totalAttachmentUsage)
                    DiagnosticRow(label = "Total Contacts Count", value = diagnostics.totalContactsCount.toString())
                    DiagnosticRow(label = "Wallet Cards Count", value = diagnostics.walletCardsCount.toString())
                }
            }
        }
    }
}

@Composable
fun SettingsListItem(
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
            shape = CircleShape,
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
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Black.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
fun SettingsValueItem(
    icon: ImageVector,
    title: String,
    value: String,
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
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(text = value, color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
