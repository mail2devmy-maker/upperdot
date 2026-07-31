package com.mail2dev.upperdot.ui.wallet_overlay

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.upperdot.ui.components.StitchTextField
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.utils.StorageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBankCardSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, String?, String?) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var swiftBic by remember { mutableStateOf("") }
    var qrPath by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val qrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = StorageUtils.copyUriToInternalStorage(context, it, "qrcodes")
            qrPath = path
        }
    }
    
    val themeColors = listOf(
        AccentCyan,
        Color(0xFF2962FF), // Blue
        Color(0xFF00BFA5), // Teal
        Color(0xFF6200EA), // Purple
        PositiveGreen,
        NegativeRed
    )
    var selectedColor by remember { mutableStateOf(themeColors[0]) }

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
                text = "New Bank Card",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Theme Color Picker
            Text(
                text = "Card Theme",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(themeColors) { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StitchTextField(
                value = bankName,
                onValueChange = { bankName = it },
                placeholder = "Bank / Wallet Name",
                leadingIcon = Icons.Default.AccountBalance,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = holderName,
                onValueChange = { holderName = it },
                placeholder = "Account Holder Name",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                placeholder = "Account Number / IBAN",
                leadingIcon = Icons.Default.Tag,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = swiftBic,
                onValueChange = { swiftBic = it },
                placeholder = "SWIFT / BIC (Optional)",
                leadingIcon = Icons.Default.Public,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Media Control
            Surface(
                onClick = { qrLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (qrPath == null) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Attach Payment QR", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(qrPath!!),
                            contentDescription = "QR Code Preview",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { qrPath = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Discard", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSave(bankName, holderName, accountNumber, selectedColor.toArgb().toLong(), swiftBic.takeIf { it.isNotEmpty() }, qrPath) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color.Black
                    ),
                    enabled = bankName.isNotEmpty() && holderName.isNotEmpty() && accountNumber.isNotEmpty()
                ) {
                    Text(text = "Save Card", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
