package com.mail2dev.upperdot.ui.onboarding

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.PrimaryYellow
import com.mail2dev.upperdot.ui.theme.Surface

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var isDialerDefault by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }
    var isBatteryIgnored by remember { mutableStateOf(false) }
    var isPermissionsGranted by remember { mutableStateOf(false) }

    // Launcher for Dialer Role
    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p } }

    // Launcher for standard permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p } }

    LaunchedEffect(Unit) {
        updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p }
    }

    // Auto-complete check
    if (isDialerDefault && isOverlayGranted && isBatteryIgnored && isPermissionsGranted) {
        SideEffect { onComplete() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Telephony Setup",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )
            Text(
                text = "UpperDot requires specific system roles to function as your primary phone app.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            SetupItem(
                title = "Default Phone App",
                description = "Required to handle and manage cellular calls.",
                isGranted = isDialerDefault,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = context.getSystemService(RoleManager::class.java)
                        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                                roleLauncher.launch(intent)
                            } else {
                                // Already held, just refresh state
                                updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p }
                            }
                        }
                    } else {
                        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
                        if (telecomManager.defaultDialerPackage != context.packageName) {
                            val intent = Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                            }
                            context.startActivity(intent)
                        } else {
                            updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p }
                        }
                    }
                }
            )

            SetupItem(
                title = "Display Over Apps",
                description = "Required to show the call screen while your phone is locked.",
                isGranted = isOverlayGranted,
                onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                }
            )

            SetupItem(
                title = "Battery Performance",
                description = "Prevents the system from killing the call service to save power.",
                isGranted = isBatteryIgnored,
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                }
            )

            SetupItem(
                title = "Core Permissions",
                description = "Access to Phone, Microphone, and Call Logs.",
                isGranted = isPermissionsGranted,
                onClick = {
                    permissionLauncher.launch(arrayOf(
                        android.Manifest.permission.CALL_PHONE,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.READ_CALL_LOG,
                        android.Manifest.permission.READ_CONTACTS
                    ))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { updateState(context) { d, o, b, p -> isDialerDefault = d; isOverlayGranted = o; isBatteryIgnored = b; isPermissionsGranted = p } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
            ) {
                Text("Refresh Status", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SetupItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = if (!isGranted) onClick else ({})
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Circle,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = description, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

private fun updateState(context: Context, onUpdate: (Boolean, Boolean, Boolean, Boolean) -> Unit) {
    val isDialer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        true
    }

    val isOverlay = Settings.canDrawOverlays(context)

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    val permissions = arrayOf(
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS
    )
    val isPerms = permissions.all {
        androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    onUpdate(isDialer, isOverlay, isBattery, isPerms)
}
