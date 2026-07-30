package com.mail2dev.upperdot.ui.auth_launchpad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.upperdot.ui.theme.PrimaryYellow

@Composable
fun AuthLaunchpadScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val showGuestWarning by viewModel.showGuestWarning.collectAsState()

    if (showGuestWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGuestWarning() },
            title = { Text("Try as Guest") },
            text = { Text("This mode will not save to the cloud. No sync will occur. Data is local-only and will be lost if the app is closed.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmGuestMode(onNavigateToDashboard) }) {
                    Text("Proceed", color = PrimaryYellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGuestWarning() }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "UpperDot",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Secure Business Card CRM",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { viewModel.onSignInWithGoogleClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Google icon would go here
                    Text(
                        text = "Sign in with Google",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.onTryAsGuestClicked() }) {
                Text(
                    text = "Try as Guest",
                    color = PrimaryYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
