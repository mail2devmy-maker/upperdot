package com.mail2dev.upperdot.ui.add_contact

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.components.StitchDropdown
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.components.WizardTabRow
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactIdentityScreen(
    onNavigateBack: () -> Unit,
    onStepSelected: (Int) -> Unit,
    viewModel: AddContactViewModel
) {
    val emails by viewModel.emails.collectAsState()
    val socialProfiles by viewModel.socialProfiles.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val tagName by viewModel.tagName.collectAsState()
    val availableGroups by viewModel.availableGroups.collectAsState()
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
                // Emails
                emails.forEachIndexed { index, email ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StitchTextField(
                            value = email,
                            onValueChange = { viewModel.onEmailChange(index, it) },
                            placeholder = if (index == 0) "Email Address" else "Additional Email",
                            leadingIcon = Icons.Default.Email,
                            modifier = Modifier.weight(1f)
                        )
                        if (index > 0) {
                            IconButton(
                                onClick = { viewModel.removeEmailField(index) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove Email",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "[ + Add Another Email ]",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addEmailField() }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Social Profiles Section
                Text(
                    text = "SOCIAL PROFILES",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                socialProfiles.forEachIndexed { index, profile ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StitchDropdown(
                            selectedOption = profile.platform,
                            options = listOf("Facebook", "Instagram", "X", "TikTok", "YouTube", "Shopee", "Lazada", "Custom"),
                            onOptionSelected = { viewModel.onSocialPlatformChange(index, it) },
                            modifier = Modifier.weight(0.4f)
                        )
                        StitchTextField(
                            value = profile.handle,
                            onValueChange = { viewModel.onSocialHandleChange(index, it) },
                            placeholder = "URL / Handle",
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "[ + Add Social Profile ]",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.addSocialProfile() }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Relationship Group Section
                Text(
                    text = "RELATIONSHIP GROUP",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group Dropdown
                StitchDropdown(
                    selectedOption = if (groupName.isEmpty()) "Assign Group" else groupName,
                    options = availableGroups.map { it.name },
                    onOptionSelected = viewModel::onGroupNameChange,
                    leadingIcon = Icons.Default.Groups,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tag Dropdown (Dependent on Group)
                val selectedGroupTags = availableGroups.find { it.name == groupName }?.tags?.map { it.name } ?: emptyList()
                val isTagEnabled = groupName.isNotEmpty() && selectedGroupTags.isNotEmpty()
                val tagLabel = when {
                    groupName.isEmpty() -> "Select Tag (Optional)"
                    selectedGroupTags.isEmpty() -> "No tags available"
                    tagName.isEmpty() -> "Select Tag (Optional)"
                    else -> tagName
                }
                
                StitchDropdown(
                    selectedOption = tagLabel,
                    options = selectedGroupTags,
                    onOptionSelected = viewModel::onTagNameChange,
                    leadingIcon = Icons.Default.Tag,
                    enabled = isTagEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
