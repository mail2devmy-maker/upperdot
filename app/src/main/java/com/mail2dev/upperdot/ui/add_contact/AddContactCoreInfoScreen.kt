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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        tint = AccentCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Form Fields
                StitchTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    placeholder = "Full Name (Required)",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                StitchTextField(
                    value = nicknames,
                    onValueChange = viewModel::onNicknamesChange,
                    placeholder = "Nicknames (Comma Separated)",
                    leadingIcon = Icons.Default.Label
                )

                Spacer(modifier = Modifier.height(16.dp))

                phoneNumbers.forEachIndexed { index, number ->
                    StitchTextField(
                        value = number,
                        onValueChange = { viewModel.onPhoneNumberChange(index, it) },
                        placeholder = if (index == 0) "Primary Phone Number" else "Additional Number",
                        leadingIcon = Icons.Default.Phone
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "[ + Add Another Number ]",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addPhoneNumber() }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WizardTabRow(
    selectedStep: Int,
    onStepSelected: (Int) -> Unit
) {
    val steps = listOf("Core Info", "Identity", "Corporate", "Financial")
    ScrollableTabRow(
        selectedTabIndex = selectedStep,
        containerColor = Color.Black,
        contentColor = AccentCyan,
        edgePadding = 24.dp,
        indicator = {},
        divider = {}
    ) {
        steps.forEachIndexed { index, title ->
            val isSelected = selectedStep == index
            Tab(
                selected = isSelected,
                onClick = { onStepSelected(index) },
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) AccentCyan else Surface,
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StitchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp)) },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            disabledContainerColor = Surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AccentCyan,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}
