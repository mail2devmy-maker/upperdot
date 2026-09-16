package com.mail2dev.upperdot.ui.dialer

import android.telephony.SubscriptionInfo
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.ui.call_history.CallHistoryViewModel
import com.mail2dev.upperdot.ui.components.TelephonyKeypad
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.StitchDesignSystem
import java.io.File

@Composable
fun DialerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddContact: (String) -> Unit,
    onNavigateToContact: (Long) -> Unit,
    viewModel: CallHistoryViewModel
) {
    var dialValue by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val favoriteContacts by viewModel.favoriteContacts.collectAsState()
    val selectedSim by viewModel.selectedSim.collectAsState()
    val availableSims by viewModel.availableSims.collectAsState()
    
    val clipboardManager = LocalClipboardManager.current
    var clipboardContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val text = clipboardManager.getText()?.text
        if (text != null && text.any { it.isDigit() }) {
            clipboardContent = text.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        }
    }

    fun handleDigitClick(digit: Char) {
        if (dialValue.text.length >= 20) return
        val digitStr = digit.toString()
        val start = dialValue.selection.start
        val end = dialValue.selection.end
        val newText = StringBuilder(dialValue.text).replace(start, end, digitStr).toString()
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Standardized Top Bar
            StitchDesignSystem.TopBar(
                title = "Dialer",
                leadingIcon = Icons.Default.Dialpad,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )

            // Results / Favorites / Identity Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    AnimatedContent(
                        targetState = dialValue.text.isEmpty(),
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "DialerContentTransition"
                    ) { isEmpty ->
                        if (isEmpty) {
                            FavoriteCarousel(
                                contacts = favoriteContacts,
                                onContactClick = { contact ->
                                    contact.phoneNumbers.firstOrNull()?.let { viewModel.makeCall(it) }
                                }
                            )
                        } else {
                            ReconstructedResultsArea(
                                dialValue = dialValue,
                                searchResults = searchResults,
                                clipboardContent = clipboardContent,
                                onPaste = { 
                                    dialValue = TextFieldValue(clipboardContent!!, TextRange(clipboardContent!!.length))
                                    clipboardContent = null
                                },
                                onAddContact = { onNavigateToAddContact(dialValue.text) },
                                onContactClick = onNavigateToContact,
                                onValueChange = { 
                                    dialValue = it
                                    viewModel.onSearchQueryChanged(it.text)
                                }
                            )
                        }
                    }
                }
            }

            // Keypad Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                TelephonyKeypad(
                    onDigitClick = { handleDigitClick(it) },
                    onLongClickZero = { 
                        val start = dialValue.selection.start
                        val end = dialValue.selection.end
                        val newText = StringBuilder(dialValue.text).replace(start, end, "+").toString()
                        val newSelection = TextRange(start + 1)
                        dialValue = TextFieldValue(newText, newSelection)
                        viewModel.onSearchQueryChanged(newText)
                    }
                )

                // Reconstructed Action Row: Uniform with In-Call Primary Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sim Selector
                    SimSelector(
                        selectedSim = selectedSim,
                        availableSims = availableSims,
                        onClick = viewModel::toggleSim
                    )

                    // Large Green Call Button - 80dp for Uniformity
                    FloatingActionButton(
                        onClick = {
                            if (dialValue.text.isNotEmpty()) {
                                viewModel.makeCall(dialValue.text)
                            }
                        },
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(Icons.Default.Call, "Dial", modifier = Modifier.size(24.dp))
                    }

                    // Backspace - Transparent and aligned
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        if (dialValue.text.isNotEmpty()) {
                            IconButton(onClick = { handleBackspace() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    "Backspace",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReconstructedResultsArea(
    dialValue: TextFieldValue,
    searchResults: List<ContactEntity>,
    clipboardContent: String?,
    onPaste: () -> Unit,
    onAddContact: () -> Unit,
    onContactClick: (Long) -> Unit,
    onValueChange: (TextFieldValue) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Identity Display (Glow Avatar)
        if (searchResults.isNotEmpty()) {
            val contact = searchResults.first()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AccentCyan.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                    
                    Surface(
                        onClick = { onContactClick(contact.id) },
                        shape = CircleShape,
                        color = Surface,
                        border = androidx.compose.foundation.BorderStroke(2.dp, AccentCyan),
                        modifier = Modifier.size(80.dp)
                    ) {
                        val displayPath = contact.thumbnailPath ?: contact.avatarPath
                        if (displayPath != null) {
                            AsyncImage(
                                model = File(displayPath),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    contact.fullName.take(1).uppercase(),
                                    color = AccentCyan,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Text(
                    text = contact.fullName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else if (dialValue.text.isNotEmpty()) {
            Text(
                text = "+ Add to connections",
                color = AccentCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAddContact() }
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            )
        }

        // 2. Centered Digits with Highlight Capsule (Uniform with In-Call Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(64.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = dialValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                ),
                cursorBrush = SolidColor(AccentCyan),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        // 3. Paste Action
        if (clipboardContent != null && dialValue.text.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = onPaste,
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ContentPaste, null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paste $clipboardContent", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FavoriteCarousel(
    contacts: List<ContactEntity>,
    onContactClick: (ContactEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (contacts.isNotEmpty()) {
            Text(
                "FAVORITES",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(contacts) { contact ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onContactClick(contact) }
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = Surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            val displayPath = contact.thumbnailPath ?: contact.avatarPath
                            if (displayPath != null) {
                                AsyncImage(
                                    model = File(displayPath),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        contact.fullName.take(1).uppercase(),
                                        color = AccentCyan,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            contact.fullName,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SimSelector(
    selectedSim: SubscriptionInfo?,
    availableSims: List<SubscriptionInfo>,
    onClick: () -> Unit
) {
    Surface(
        onClick = if (availableSims.size > 1) onClick else ({}),
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SimCard,
                contentDescription = null,
                tint = if (availableSims.isNotEmpty()) AccentCyan else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (selectedSim != null) "SIM ${selectedSim.simSlotIndex + 1}" else "NONE",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            if (availableSims.size > 1) {
                Text(
                    text = "SWITCH",
                    color = AccentCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
