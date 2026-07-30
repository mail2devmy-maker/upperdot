package com.mail2dev.upperdot.ui.new_cash_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.components.StitchDropdown
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCashTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (String, Boolean, String, String, String) -> Unit,
    contactNames: List<String>,
    initialContact: String? = null
) {
    var selectedContact by remember { mutableStateOf(initialContact ?: "") }
    var isRevenue by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    
    val currentDateTime = "Jul 30, 2026"

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
                text = "New Cash Transaction",
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

            // Transaction Type Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                TransactionTypeItem(
                    title = "REVENUE",
                    isSelected = isRevenue,
                    activeColor = PositiveGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { isRevenue = true }
                )
                TransactionTypeItem(
                    title = "EXPENSE",
                    isSelected = !isRevenue,
                    activeColor = NegativeRed,
                    modifier = Modifier.weight(1f),
                    onClick = { isRevenue = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Amount Split Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StitchTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Title",
                    modifier = Modifier.weight(0.6f)
                )
                StitchTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "Amount ($)",
                    modifier = Modifier.weight(0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = detail,
                onValueChange = { detail = it },
                placeholder = "Detail / Notes",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = currentDateTime, color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice and Camera Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                        .clickable { /* Trigger Camera Picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = AccentCyan)
                }

                // Voice Recording
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { /* Hold to record */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Memo", tint = AccentCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(selectedContact, isRevenue, title, amount, detail) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRevenue) PositiveGreen else AccentCyan,
                    contentColor = Color.Black
                ),
                enabled = selectedContact.isNotEmpty() && title.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text(text = "Finalize Transaction", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TransactionTypeItem(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
