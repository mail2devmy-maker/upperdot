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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.R
import com.mail2dev.upperdot.ui.components.StitchDropdown
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.components.WizardTabRow
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import com.mail2dev.upperdot.util.ContactUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactIdentityScreen(
    onNavigateBack: () -> Unit,
    onStepSelected: (Int) -> Unit,
    viewModel: AddContactViewModel,
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
                colors = TopAppBarDefaults.topAppBarColors(
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
                // Emails Section
                Text(
                    text = "EMAIL ADDRESSES",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                ) {
                    Column {
                        emails.forEachIndexed { index, email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StitchTextField(
                                    value = email,
                                    onValueChange = { viewModel.onEmailChange(index, it) },
                                    placeholder = if (index == 0) "Primary Email" else "Additional Email",
                                    leadingIcon = Icons.Default.Email,
                                    modifier = Modifier.weight(1f),
                                    showBorder = false,
                                    containerColor = Color.Transparent
                                )
                                if (index > 0) {
                                    IconButton(onClick = { viewModel.removeEmailField(index) }) {
                                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            if (index < (emails.size - 1)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color.DarkGray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                GhostAddButton(
                    text = "Add email",
                    onClick = viewModel::addEmailField
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Social Profiles Section
                Text(
                    text = "SOCIAL PROFILES",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                ) {
                    Column {
                        socialProfiles.forEachIndexed { index, profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PlatformIconPicker(
                                    selectedPlatform = profile.platform,
                                    onPlatformSelected = { viewModel.onSocialPlatformChange(index, it) }
                                )
                                StitchTextField(
                                    value = profile.handle,
                                    onValueChange = { viewModel.onSocialHandleChange(index, it) },
                                    placeholder = "URL / Handle",
                                    modifier = Modifier.weight(1f),
                                    showBorder = false,
                                    containerColor = Color.Transparent
                                )
                            }
                            if (index < (socialProfiles.size - 1)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color.DarkGray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                GhostAddButton(
                    text = "Add social profile",
                    onClick = viewModel::addSocialProfile
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
                    selectedOption = groupName.ifEmpty { "Assign Group" },
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

@Composable
private fun PlatformIconPicker(
    selectedPlatform: String,
    onPlatformSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val platforms = listOf("WhatsApp", "Facebook", "Instagram", "X", "TikTok", "YouTube", "Shopee", "Lazada", "Telegram", "Custom")

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(ContactUtils.getSocialPlatformDrawable(selectedPlatform)),
                    contentDescription = selectedPlatform,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface)
        ) {
            platforms.forEach { platform ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(ContactUtils.getSocialPlatformDrawable(platform)),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(platform, color = Color.White)
                        }
                    },
                    onClick = {
                        onPlatformSelected(platform)
                        expanded = false
                    }
                )
            }
        }
    }
}
