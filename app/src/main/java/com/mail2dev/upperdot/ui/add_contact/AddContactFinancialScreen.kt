package com.mail2dev.upperdot.ui.add_contact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.components.WizardTabRow
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactFinancialScreen(
    onNavigateBack: () -> Unit,
    onStepSelected: (Int) -> Unit,
    viewModel: AddContactViewModel
) {
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val savedBanks by viewModel.savedBanks.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val showDiscardDialog by viewModel.showDiscardDialog.collectAsState()

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDiscardDialog() },
            title = { Text("Discard Changes?") },
            text = { Text("Are you sure you want to discard all inputs? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDiscardDialog() }) {
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ADD CONTACT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onDiscardRequest() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveContact(onNavigateBack) },
                containerColor = Color.Transparent,
                contentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .border(1.dp, AccentCyan, CircleShape)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            WizardTabRow(
                selectedStep = currentStep,
                onStepSelected = onStepSelected
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "BANK VAULT",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                bankAccounts.forEachIndexed { index, account ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                    ) {
                        Column {
                            // Header for Account
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECURE ACCOUNT ${if (bankAccounts.size > 1) index + 1 else ""}",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (index > 0) {
                                    IconButton(
                                        onClick = { viewModel.removeBankAccount(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            BankAutoSuggestInput(
                                value = account.bankName,
                                onValueChange = { viewModel.onBankNameChange(index, it) },
                                suggestions = savedBanks,
                                onAddCustom = viewModel::onAddCustomBank,
                                onDeleteBank = viewModel::onDeleteBank,
                                onRenameBank = viewModel::onRenameBank,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.DarkGray.copy(alpha = 0.2f)
                            )

                            StitchTextField(
                                value = account.accountNumber,
                                onValueChange = { viewModel.onBankAccountNumberChange(index, it) },
                                placeholder = "Account Number / IBAN",
                                leadingIcon = Icons.Default.Tag,
                                showBorder = false,
                                containerColor = Color.Transparent
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.DarkGray.copy(alpha = 0.2f)
                            )

                            StitchTextField(
                                value = account.holderName,
                                onValueChange = { viewModel.onBankHolderNameChange(index, it) },
                                placeholder = "Account Holder Name",
                                leadingIcon = Icons.Default.Person,
                                showBorder = false,
                                containerColor = Color.Transparent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                GhostAddButton(
                    text = "Add bank account",
                    onClick = viewModel::addBankAccount
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankAutoSuggestInput(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    onAddCustom: (String) -> Unit,
    onDeleteBank: (String) -> Unit,
    onRenameBank: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isEmpty()) emptyList()
        else suggestions.filter { it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true) }
    }

    if (showRenameDialog != null) {
        var newName by remember { mutableStateOf(showRenameDialog!!) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Bank") },
            text = {
                StitchTextField(
                    value = newName,
                    onValueChange = { newName = it.uppercase() },
                    placeholder = "New Name",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenameBank(showRenameDialog!!, newName)
                    showRenameDialog = null
                }) {
                    Text("Save", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Surface
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded && (filteredSuggestions.isNotEmpty() || (value.isNotEmpty() && !suggestions.any { it.equals(value, ignoreCase = true) })),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        StitchTextField(
            value = value,
            onValueChange = {
                onValueChange(it.uppercase())
                expanded = true
            },
            placeholder = "BANK / WALLET NAME",
            leadingIcon = Icons.Default.AccountBalance,
            showBorder = false,
            containerColor = Color.Transparent,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface)
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(suggestion, color = Color.White, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = { showRenameDialog = suggestion }) {
                                    Icon(Icons.Default.Edit, "Rename", tint = AccentCyan, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteBank(suggestion) }) {
                                    Icon(Icons.Default.Close, "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }

            // Custom "Add" option
            if (value.isNotEmpty() && !suggestions.any { it.equals(value, ignoreCase = true) }) {
                DropdownMenuItem(
                    text = { Text("+ Add '$value'", color = AccentCyan, fontWeight = FontWeight.Bold) },
                    onClick = {
                        onAddCustom(value)
                        onValueChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GhostAddButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AccentCyan)
        }
    }
}
