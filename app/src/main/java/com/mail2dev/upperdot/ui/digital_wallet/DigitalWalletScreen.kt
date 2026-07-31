package com.mail2dev.upperdot.ui.digital_wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.theme.*
import com.mail2dev.upperdot.ui.wallet_overlay.NewBankCardSheet

fun formatAccountNumber(number: String): String {
    return number.replace(" ", "").chunked(4).joinToString(" ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalWalletScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    viewModel: DigitalWalletViewModel = viewModel()
) {
    val bankCards by viewModel.bankCards.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val showAddCardSheet by viewModel.showAddCardSheet.collectAsState()
    val editingCard by viewModel.editingCard.collectAsState()

    if (showAddCardSheet) {
        NewBankCardSheet(
            onDismiss = viewModel::dismissAddCardSheet,
            onSave = { bank, holder, number, color, swift, qr -> 
                viewModel.saveCard(bank, holder, number, color, swift, qr)
            },
            editingCard = editingCard
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Digital Wallet Management", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.onAddCardClicked(
                        onSuccess = { /* No-op, sheet handled by VM state */ },
                        onLimitExceeded = onNavigateToPlans
                    )
                },
                containerColor = AccentCyan,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add New Card", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tier Notification Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Manage your payment cards and QR codes. Free users can store up to 1 card.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(bankCards, key = { it.id }) { card ->
                BankCardItem(
                    card = card,
                    onEdit = { viewModel.prepareEditCard(card.id) },
                    onDelete = { viewModel.onDeleteCard(card.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // FAB Spacing
            }
        }
    }
}

@Composable
fun BankCardItem(
    card: BankCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bankColor = Color(card.themeColor.toInt())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = bankColor.copy(alpha = 0.30f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface // #1E1E1E
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bankColor.copy(alpha = 0.20f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = bankColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = card.bankName.uppercase(),
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatAccountNumber(card.accountNumber),
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 30.dp)
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentCyan)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NegativeRed)
                    }
                }
            }
        }
    }
}
