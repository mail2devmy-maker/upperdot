package com.mail2dev.upperdot.ui.data_vault

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.navigation.NavController
import com.mail2dev.upperdot.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class DataVaultRestoreType {
    CLOUD, LOCAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVaultManagementScreen(
    navController: NavController,
    viewModel: DataVaultViewModel
) {
    val context = LocalContext.current
    val isSyncing by viewModel.isSyncing.collectAsState()
    val vcfImportState by viewModel.vcfImportState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestoreConfirm by remember { mutableStateOf<DataVaultRestoreType?>(null) }

    BackHandler(enabled = true) {
        navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            scope.launch {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    viewModel.exportDatabase(context.filesDir, stream)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    viewModel.importDatabase(context.filesDir, stream)
                }
            }
        }
    }

    val vcfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.onVcfSelected(stream)
            }
        }
    }

    if (showRestoreConfirm != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = null },
            title = { Text("Overwrite Local Data?", color = Color.White) },
            text = {
                Text(
                    "Restoring data will overwrite your current local database. Do you want to proceed?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val type = showRestoreConfirm
                        showRestoreConfirm = null
                        if (type == DataVaultRestoreType.CLOUD) {
                            viewModel.onRestoreClicked()
                        } else {
                            importLauncher.launch(arrayOf("application/zip"))
                        }
                    }
                ) {
                    Text("Proceed", color = NegativeRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Surface
        )
    }

    if (vcfImportState is VcfImportState.Conflict) {
        val conflict = vcfImportState as VcfImportState.Conflict
        AlertDialog(
            onDismissRequest = { viewModel.dismissVcfDialog() },
            title = { Text("Import Conflicts Found", color = Color.White) },
            text = {
                Text(
                    "We found ${conflict.conflicts.size} contacts that already exist in your directory. How would you like to resolve these?",
                    color = Color.White
                )
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.resolveVcfConflicts("OVERWRITE") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) { Text("Overwrite Existing") }

                    Button(
                        onClick = { viewModel.resolveVcfConflicts("DUPLICATE") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = Color.White)
                    ) { Text("Keep Both (Duplicate)") }

                    TextButton(
                        onClick = { viewModel.resolveVcfConflicts("SKIP") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Skip Conflicts", color = AccentCyan) }
                }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Cloud Vault", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Card Deck A: CLOUD VAULT STORAGE
            item {
                Column {
                    Text(
                        text = "CLOUD VAULT STORAGE",
                        color = Color(0xFF9575CD), // Accent Purple
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Upload to Cloud
                                Button(
                                    onClick = viewModel::onBackupClicked,
                                    enabled = !isSyncing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload to Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Download Cloud
                                OutlinedButton(
                                    onClick = { showRestoreConfirm = DataVaultRestoreType.CLOUD },
                                    enabled = !isSyncing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Download Cloud", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card Deck B: LOCAL FILE PORTABILITY
            item {
                Column {
                    Text(
                        text = "LOCAL FILE PORTABILITY",
                        color = Color(0xFF9575CD), // Accent Purple
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column {
                            // Item 1: Import VCF Contacts
                            DataVaultListItem(
                                icon = Icons.Default.ContactPage,
                                title = "Import VCF Contacts",
                                subtitle = "Load external .vcf contact files",
                                onClick = { vcfPickerLauncher.launch(arrayOf("text/vcard", "text/x-vcard")) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.DarkGray.copy(alpha = 0.3f)
                            )
                            // Item 2: Export Local ZIP Backup
                            DataVaultListItem(
                                icon = Icons.Default.Folder,
                                badgeIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                title = "Export Local ZIP Backup",
                                subtitle = "Save a secure local snapshot file",
                                onClick = { exportLauncher.launch("upperdot_backup_${System.currentTimeMillis()}.zip") }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.DarkGray.copy(alpha = 0.3f)
                            )
                            // Item 3: Import Local ZIP Backup
                            DataVaultListItem(
                                icon = Icons.Default.Folder,
                                badgeIcon = Icons.AutoMirrored.Filled.ArrowBack,
                                title = "Import Local ZIP Backup",
                                subtitle = "Overwrite current data from ZIP file",
                                onClick = { showRestoreConfirm = DataVaultRestoreType.LOCAL }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataVaultListItem(
    icon: ImageVector,
    badgeIcon: ImageVector? = null,
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
        // Leading Icon Box (Folder/Page + Small Arrow Badge on bottom-right of icon)
        Box(modifier = Modifier.size(40.dp)) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            badgeIcon?.let { badge ->
                Surface(
                    shape = CircleShape,
                    color = AccentCyan,
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = badge,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Subtitle Text
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }

        // Standard Right Navigation Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(20.dp)
        )
    }
}