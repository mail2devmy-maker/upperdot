package com.mail2dev.upperdot.ui.add_contact

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.upperdot.ui.components.*
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import com.mail2dev.upperdot.util.ContactUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    editId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: AddContactViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onAvatarChanged(it.toString())
            }
        }
    )

    LaunchedEffect(editId) {
        viewModel.loadContact(editId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddContactEvent.ValidationError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                AddContactEvent.SaveSuccess -> {
                    // Handled by onNavigateBack
                }
            }
        }
    }

    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDiscardDialog() },
            title = { Text("Discard Changes?") },
            text = { Text("Are you sure you want to discard all inputs? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetForm()
                    onNavigateBack()
                }) {
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

    if (uiState.showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDuplicateWarning() },
            title = { Text("Duplicate Detected") },
            text = { 
                Text("A contact named '${uiState.duplicateConflict?.fullName}' already exists. Would you like to update that record or save this as a separate entry?") 
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            // Update existing by loading it first
                            uiState.duplicateConflict?.let { 
                                viewModel.loadContact(it.id)
                                viewModel.dismissDuplicateWarning()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) {
                        Text("Update Existing")
                    }
                    Button(
                        onClick = { viewModel.saveContact(onNavigateBack, forceSave = true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = Color.White)
                    ) {
                        Text("Save as Duplicate")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDuplicateWarning() }) {
                    Text("Cancel", color = Color.Gray)
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
                        if (viewModel.isEditMode) "EDIT CONTACT" else "ADD CONTACT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onDiscardRequest(onNavigateBack) }) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.ime)) {
                    Button(
                        onClick = { viewModel.saveContact(onNavigateBack) },
                        enabled = uiState.fullName.isNotBlank() && !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Save Contact Record", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 8.dp) // Extra spacing to prevent overlap with button
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Picker
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(2.dp, AccentCyan, CircleShape)
                        .clickable { 
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.avatarPath != null) {
                        AsyncImage(
                            model = uiState.avatarPath,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Change Avatar",
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Core Info Section
            StitchTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                placeholder = "Full Name (Required)",
                leadingIcon = Icons.Default.Person,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError
            )

            StitchTextField(
                value = uiState.nicknames,
                onValueChange = viewModel::onNicknamesChange,
                placeholder = "Nicknames (Comma Separated)",
                leadingIcon = Icons.Default.Label
            )

            StitchTextField(
                value = uiState.remark,
                onValueChange = viewModel::onRemarkChange,
                placeholder = "Remark / Quick Note (Optional)",
                leadingIcon = Icons.Default.Note,
                singleLine = false,
                minLines = 3
            )

            Text("PHONE NUMBERS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            uiState.phoneNumbers.forEachIndexed { index, phone ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StitchTextField(
                        value = phone,
                        onValueChange = { viewModel.onPhoneNumberChange(index, it) },
                        placeholder = if (index == 0) "Primary Phone" else "Secondary Phone",
                        leadingIcon = Icons.Default.Phone,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    if (index > 0) {
                        IconButton(onClick = { viewModel.removePhoneNumber(index) }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            TextButton(onClick = viewModel::addPhoneNumber, colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add phone number", fontSize = 14.sp)
                }
            }

            // --- Expandable Sections ---

            ExpandableSection(
                title = "IDENTITY & SOCIAL",
                isExpanded = uiState.isIdentityExpanded,
                onToggle = viewModel::toggleIdentityExpanded
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("EMAIL ADDRESSES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    uiState.emails.forEachIndexed { index, email ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StitchTextField(
                                value = email,
                                onValueChange = { viewModel.onEmailChange(index, it) },
                                placeholder = if (index == 0) "Primary Email" else "Additional Email",
                                leadingIcon = Icons.Default.Email,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            if (index > 0) {
                                IconButton(onClick = { viewModel.removeEmailField(index) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                    TextButton(onClick = viewModel::addEmailField, colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add email", fontSize = 14.sp)
                        }
                    }

                    Text("SOCIAL PROFILES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    uiState.socialProfiles.forEachIndexed { index, profile ->
                        SocialProfileRow(
                            profile = profile,
                            onPlatformChange = { viewModel.onSocialPlatformChange(index, it) },
                            onHandleChange = { viewModel.onSocialHandleChange(index, it) }
                        )
                    }
                    TextButton(onClick = viewModel::addSocialProfile, colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add social profile", fontSize = 14.sp)
                        }
                    }

                    Text("RELATIONSHIP GROUP", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    val availableGroups by viewModel.availableGroups.collectAsState()
                    
                    StitchDropdown(
                        selectedOption = uiState.groupName.ifEmpty { "Assign Group" },
                        options = availableGroups.map { it.name },
                        onOptionSelected = viewModel::onGroupNameChange,
                        leadingIcon = Icons.Default.Groups,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val selectedGroupTags = availableGroups.find { it.name == uiState.groupName }?.tags?.map { it.name } ?: emptyList()
                    val isTagEnabled = uiState.groupName.isNotEmpty() && selectedGroupTags.isNotEmpty()
                    val tagLabel = when {
                        uiState.groupName.isEmpty() -> "Select Tag (Optional)"
                        selectedGroupTags.isEmpty() -> "No tags available"
                        uiState.tagName.isEmpty() -> "Select Tag (Optional)"
                        else -> uiState.tagName
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

            ExpandableSection(
                title = "CORPORATE INFO",
                isExpanded = uiState.isCorporateExpanded,
                onToggle = viewModel::toggleCorporateExpanded
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StitchTextField(
                        value = uiState.companyName,
                        onValueChange = viewModel::onCompanyNameChange,
                        placeholder = "Company Name",
                        leadingIcon = Icons.Default.Business
                    )
                    
                    val categories = listOf("Services", "Retail", "Technology", "Manufacturing", "Finance", "Healthcare", "Education", "Food & Beverage", "Other")
                    StitchDropdown(
                        selectedOption = uiState.businessCategory,
                        options = categories,
                        onOptionSelected = viewModel::onBusinessCategoryChange,
                        label = "Business Category",
                        leadingIcon = Icons.Default.Category,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StitchTextField(
                        value = uiState.officeAddress,
                        onValueChange = viewModel::onOfficeAddressChange,
                        placeholder = "Physical Office Address",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
            }

            ExpandableSection(
                title = "BANK VAULT / FINANCIAL",
                isExpanded = uiState.isFinancialExpanded,
                onToggle = viewModel::toggleFinancialExpanded
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    uiState.bankAccounts.forEachIndexed { index, account ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("SECURE ACCOUNT", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (index > 0) {
                                        IconButton(onClick = { viewModel.removeBankAccount(index) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                
                                StitchTextField(
                                    value = account.bankName,
                                    onValueChange = { viewModel.onBankNameChange(index, it) },
                                    placeholder = "Bank / Wallet Name",
                                    leadingIcon = Icons.Default.AccountBalance
                                )

                                val clipboard = LocalClipboardManager.current
                                StitchTextField(
                                    value = account.accountNumber,
                                    onValueChange = { viewModel.onBankAccountNumberChange(index, it) },
                                    placeholder = "Account Number / IBAN",
                                    leadingIcon = Icons.Default.Tag,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            clipboard.getText()?.text?.let { viewModel.onBankAccountNumberChange(index, it) }
                                        }) {
                                            Icon(Icons.Default.ContentPaste, "Paste", tint = AccentCyan, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )

                                StitchTextField(
                                    value = account.holderName,
                                    onValueChange = { viewModel.onBankHolderNameChange(index, it) },
                                    placeholder = "Account Holder Name",
                                    leadingIcon = Icons.Default.PersonOutline
                                )
                            }
                        }
                    }
                    TextButton(onClick = viewModel::addBankAccount, colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add bank account", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isExpanded) AccentCyan else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = if (isExpanded) AccentCyan else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                content()
            }
        }
        
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
    }
}

@Composable
fun SocialProfileRow(
    profile: SocialProfile,
    onPlatformChange: (String) -> Unit,
    onHandleChange: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var showDropdown by remember { mutableStateOf(false) }
    val platforms = listOf("WhatsApp", "Facebook", "Instagram", "X", "TikTok", "YouTube", "LinkedIn", "Telegram", "Custom")

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.35f)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { showDropdown = true },
                color = Color.Black.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        painter = painterResource(ContactUtils.getSocialPlatformDrawable(profile.platform)),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(profile.platform, color = Color.White, fontSize = 11.sp, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
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
                            onPlatformChange(platform)
                            showDropdown = false
                        }
                    )
                }
            }
        }

        StitchTextField(
            value = profile.handle,
            onValueChange = onHandleChange,
            placeholder = "URL / Handle",
            modifier = Modifier.weight(0.65f),
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.getText()?.text?.let { onHandleChange(it) }
                }) {
                    Icon(Icons.Default.ContentPaste, "Paste", tint = AccentCyan, modifier = Modifier.size(18.dp))
                }
            }
        )
    }
}
