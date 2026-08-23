package com.mail2dev.upperdot.ui.dialer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mail2dev.upperdot.ui.call_history.CallHistoryViewModel
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.util.TelephonyUtils

@Composable
fun DialerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddContact: (String) -> Unit,
    onNavigateToContact: (Long) -> Unit,
    viewModel: CallHistoryViewModel
) {
    var dialValue by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var clipboardContent by remember { mutableStateOf<String?>(null) }

    // Check clipboard on launch
    LaunchedEffect(Unit) {
        val text = clipboardManager.getText()?.text
        if (text != null && text.any { it.isDigit() }) {
            // Basic validation: must contain digits
            clipboardContent = text.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted && dialValue.text.isNotEmpty()) {
            TelephonyUtils.placeOutgoingCall(context, dialValue.text)
        }
    }

    fun handleDigitClick(digit: String) {
        if (dialValue.text.length >= 20) return
        
        val start = dialValue.selection.start
        val end = dialValue.selection.end
        val newText = StringBuilder(dialValue.text).replace(start, end, digit).toString()
        val newSelection = TextRange(start + 1)
        dialValue = TextFieldValue(newText, newSelection)
        viewModel.onSearchQueryChanged(newText)
    }

    fun handleBackspace() {
        var newText = dialValue.text
        var newSelection = dialValue.selection
        
        if (dialValue.selection.length > 0) {
            val start = dialValue.selection.start
            val end = dialValue.selection.end
            newText = StringBuilder(dialValue.text).delete(start, end).toString()
            newSelection = TextRange(start)
        } else if (dialValue.selection.start > 0) {
            val index = dialValue.selection.start
            newText = StringBuilder(dialValue.text).deleteCharAt(index - 1).toString()
            newSelection = TextRange(index - 1)
        }
        dialValue = TextFieldValue(newText, newSelection)
        viewModel.onSearchQueryChanged(newText)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Dialer", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Contact Action Pills
            if (dialValue.text.isNotEmpty()) {
                ActionPill(
                    icon = Icons.Default.PersonAdd,
                    text = "Create new contact",
                    onClick = { onNavigateToAddContact(dialValue.text) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Interactive Number Display
            BasicTextField(
                value = dialValue,
                onValueChange = { 
                    dialValue = it
                    viewModel.onSearchQueryChanged(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textStyle = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(AccentCyan),
                readOnly = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // Search Results Chips
            if (searchResults.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(searchResults) { contact ->
                        SuggestionChip(
                            onClick = {
                                contact.phoneNumbers.firstOrNull()?.let { num ->
                                    val requiredPermissions = arrayOf(
                                        Manifest.permission.CALL_PHONE,
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.READ_PHONE_STATE
                                    )
                                    val missingPermissions = requiredPermissions.filter {
                                        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                                    }
                                    if (missingPermissions.isEmpty()) {
                                        viewModel.makeCall(num)
                                    } else {
                                        dialValue = TextFieldValue(num, TextRange(num.length))
                                        permissionLauncher.launch(requiredPermissions)
                                    }
                                }
                            },
                            label = { 
                                Text(
                                    text = contact.fullName,
                                    color = AccentCyan,
                                    fontSize = 12.sp
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Surface
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = AccentCyan.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Clipboard Paste Chip
            if (clipboardContent != null && dialValue.text.isEmpty()) {
                Surface(
                    onClick = { 
                        dialValue = TextFieldValue(clipboardContent!!, TextRange(clipboardContent!!.length))
                        clipboardContent = null
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Paste $clipboardContent", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Dial Pad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("*", "0", "#")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        DialButton(
                            text = key,
                            onClick = { handleDigitClick(key) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(72.dp))

                FloatingActionButton(
                    onClick = {
                        if (dialValue.text.isNotEmpty()) {
                            val requiredPermissions = arrayOf(
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_PHONE_STATE
                            )
                            val missingPermissions = requiredPermissions.filter {
                                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                            }
                            if (missingPermissions.isEmpty()) {
                                TelephonyUtils.placeOutgoingCall(context, dialValue.text)
                            } else {
                                permissionLauncher.launch(requiredPermissions)
                            }
                        }
                    },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Call, "Dial", modifier = Modifier.size(32.dp))
                }

                IconButton(
                    onClick = { handleBackspace() },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        "Backspace",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = AccentCyan.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AccentCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DialButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Surface,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
