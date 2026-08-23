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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.components.WizardTabRow
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactCoreInfoScreen(
    onNavigateBack: () -> Unit,
    onStepSelected: (Int) -> Unit,
    viewModel: AddContactViewModel
) {
    val fullName by viewModel.fullName.collectAsState()
    val nicknames by viewModel.nicknames.collectAsState()
    val phoneNumbers by viewModel.phoneNumbers.collectAsState()
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Picker
                val initials = remember(fullName) {
                    val names = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    when {
                        names.isEmpty() -> ""
                        names.size == 1 -> names[0].take(1).uppercase()
                        else -> (names[0].take(1) + names[1].take(1)).uppercase()
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, AccentCyan, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .clickable { /* Trigger Image Picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    if (initials.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Text(
                            text = initials,
                            color = AccentCyan,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Standalone Core Info Fields
                StitchTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    placeholder = "Full Name (Required)",
                    leadingIcon = Icons.Default.Person,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                StitchTextField(
                    value = nicknames,
                    onValueChange = viewModel::onNicknamesChange,
                    placeholder = "Nicknames (Comma Separated)",
                    leadingIcon = Icons.Default.Label,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Numbers Card
                Text(
                    text = "PHONE NUMBERS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                ) {
                    Column {
                        phoneNumbers.forEachIndexed { index, number ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StitchTextField(
                                    value = number,
                                    onValueChange = { viewModel.onPhoneNumberChange(index, it) },
                                    placeholder = if (index == 0) "Primary Phone" else "Additional Number",
                                    leadingIcon = Icons.Default.Phone,
                                    modifier = Modifier.weight(1f),
                                    showBorder = false,
                                    containerColor = Color.Transparent
                                )
                                if (index > 0) {
                                    IconButton(onClick = { viewModel.removePhoneNumber(index) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove Number",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            if (index < phoneNumbers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color.DarkGray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                GhostAddButton(
                    text = "Add phone number",
                    onClick = viewModel::addPhoneNumber
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
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AccentCyan)
        }
    }
}
