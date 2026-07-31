package com.mail2dev.upperdot.ui.wallet_overlay

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.mail2dev.upperdot.ui.digital_wallet.BankCard
import com.mail2dev.upperdot.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWalletOverlaySheet(
    onDismiss: () -> Unit,
    onNavigateToManagement: () -> Unit,
    onNavigateToPlans: () -> Unit,
    bankCards: List<BankCard>,
    isPremium: Boolean
) {
    val pagerState = rememberPagerState(pageCount = { 
        if (isPremium) bankCards.size else minOf(bankCards.size, 1) + 1 
    })
    val clipboardManager = LocalClipboardManager.current

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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Quick Wallet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(
                    onClick = {
                        onDismiss()
                        onNavigateToManagement()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { page ->
                if (!isPremium && page == minOf(bankCards.size, 1)) {
                    PremiumPromptCard(onNavigateToPlans)
                } else {
                    val card = bankCards.getOrNull(page)
                    if (card != null) {
                        QuickCardDisplay(
                            card = card,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(card.accountNumber))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCardDisplay(
    card: BankCard,
    onCopy: () -> Unit
) {
    var showFullScreenQr by remember { mutableStateOf(false) }

    if (showFullScreenQr) {
        FullScreenQrDialog(
            qrPath = card.qrImagePath,
            onDismiss = { showFullScreenQr = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = card.bankName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = card.cardHolderName, color = TextSecondary, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        // White QR Card Template
        Surface(
            modifier = Modifier
                .size(240.dp)
                .clickable { showFullScreenQr = true },
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (card.qrImagePath != null) {
                    AsyncImage(
                        model = File(card.qrImagePath),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.size(160.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clipboard Copy Pill
        Surface(
            onClick = onCopy,
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = card.accountNumber, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Relocated Share Button
        Surface(
            shape = CircleShape,
            color = AccentCyan,
            modifier = Modifier.size(48.dp).clickable { /* TODO: Implement Share */ }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FullScreenQrDialog(
    qrPath: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        
        window?.let {
            val params = it.attributes
            params.screenBrightness = 1.0f
            it.attributes = params
        }
        
        onDispose {
            window?.let {
                val params = it.attributes
                params.screenBrightness = originalBrightness
                it.attributes = params
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            if (qrPath != null) {
                AsyncImage(
                    model = File(qrPath),
                    contentDescription = "Full Screen QR",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(280.dp)
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 24.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun PremiumPromptCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Stars, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Premium Only", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Upgrade to store and swipe through unlimited business cards.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)
            )
        }
    }
}
