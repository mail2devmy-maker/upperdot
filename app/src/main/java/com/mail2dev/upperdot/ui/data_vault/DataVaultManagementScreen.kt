package com.mail2dev.upperdot.ui.data_vault

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.app_settings.SettingsListItem
import com.mail2dev.upperdot.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVaultManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataVaultViewModel
) {
    val context = LocalContext.current
    val cloudMetadata by viewModel.cloudMetadata.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val vcfImportState by viewModel.vcfImportState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

            // Card Deck A: Cloud Vault Sync
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (cloudMetadata.exists) 
                                    "☁️ Available Cloud Backup: ${cloudMetadata.lastModified} (${cloudMetadata.size})"
                                else "☁️ No Cloud Backup Found",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = viewModel::onRestoreClicked,
                                enabled = cloudMetadata.exists && !isSyncing,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                            ) {
                                Text("⬇️ Restore Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = viewModel::onBackupClicked,
                                enabled = !isSyncing,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("⬆️ Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Card Deck B: Local File Portability
            item {
                Column {
                    Text(
                        text = "LOCAL FILE PORTABILITY",
                        color = Color(0xFF9575CD), // Purple
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column {
                            SettingsListItem(
                                icon = Icons.Default.FileUpload,
                                title = "Import VCF Contacts",
                                subtitle = "Load external .vcf contact files",
                                onClick = { vcfPickerLauncher.launch(arrayOf("text/vcard", "text/x-vcard")) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                            SettingsListItem(
                                icon = Icons.Default.Inventory,
                                title = "Export ZIP Backup",
                                subtitle = "Save a secure local snapshot",
                                onClick = { exportLauncher.launch("upperdot_backup_${System.currentTimeMillis()}.zip") }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
                            SettingsListItem(
                                icon = Icons.Default.Unarchive,
                                title = "Import ZIP Restore",
                                subtitle = "Overwrite local data with ZIP",
                                onClick = { importLauncher.launch(arrayOf("application/zip")) }
                            )
                        }
                    }
                }
            }
        }
    }
}
