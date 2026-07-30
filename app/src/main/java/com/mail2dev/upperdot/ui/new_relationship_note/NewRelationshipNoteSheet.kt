package com.mail2dev.upperdot.ui.new_relationship_note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRelationshipNoteSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    contactNames: List<String>,
    initialContact: String? = null
) {
    var selectedContact by remember { mutableStateOf(initialContact ?: "") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    // In a real app, these would come from the ViewModel
    val currentDateTime = "Jul 30, 2026 • 12:51 AM"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Relationship Note",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            StitchDropdown(
                selectedOption = if (selectedContact.isEmpty()) "Select Contact (Mandatory)" else selectedContact,
                options = contactNames,
                onOptionSelected = { selectedContact = it },
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Note Title",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = "Content",
                singleLine = false,
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamp Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = currentDateTime, color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice and Attachments Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Attachments", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                            .clickable { /* Trigger Picker */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Attachment", tint = AccentCyan)
                    }
                }

                // Voice Recording Component
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { /* Handle Hold to Record */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = AccentCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(selectedContact, title, content) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color.Black
                ),
                enabled = selectedContact.isNotEmpty() && title.isNotEmpty()
            ) {
                Text(text = "Save Relationship Note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
