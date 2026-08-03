package com.mail2dev.upperdot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.mail2dev.upperdot.ui.add_contact.AddContactCoreInfoScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactCorporateScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactFinancialScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactIdentityScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactViewModel
import com.mail2dev.upperdot.ui.app_settings.AdvancedSettingsScreen
import com.mail2dev.upperdot.ui.app_settings.AdvancedSettingsViewModel
import com.mail2dev.upperdot.ui.auth_launchpad.AuthLaunchpadScreen
import com.mail2dev.upperdot.ui.auth_launchpad.AuthViewModel
import com.mail2dev.upperdot.ui.call_history.CallHistoryScreen
import com.mail2dev.upperdot.ui.connections_list.ConnectionsListScreen
import com.mail2dev.upperdot.ui.connections_list.ConnectionsListViewModel
import com.mail2dev.upperdot.ui.dialer.DialerScreen
import com.mail2dev.upperdot.ui.onboarding.OnboardingScreen
import com.mail2dev.upperdot.ui.data_vault.DataVaultManagementScreen
import com.mail2dev.upperdot.ui.data_vault.DataVaultViewModel
import com.mail2dev.upperdot.ui.digital_wallet.DigitalWalletScreen
import com.mail2dev.upperdot.ui.digital_wallet.DigitalWalletViewModel
import com.mail2dev.upperdot.ui.insights.InsightTab
import com.mail2dev.upperdot.ui.insights.InsightsScreen
import com.mail2dev.upperdot.ui.insights.InsightsViewModel
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailScreen
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailViewModel
import com.mail2dev.upperdot.ui.profile_settings.MyProfileSettingsScreen
import com.mail2dev.upperdot.ui.profile_settings.ProfileSettingsViewModel
import com.mail2dev.upperdot.ui.relationship_hierarchy.RelationshipHierarchyScreen
import com.mail2dev.upperdot.ui.relationship_hierarchy.RelationshipHierarchyViewModel
import com.mail2dev.upperdot.ui.theme.UpperDotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpperDotTheme {
                CallPermissionHandler()
                RootNavigation()
            }
        }
    }
}

@Composable
fun CallPermissionHandler() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val prefs = context.getSharedPreferences("upperdot_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("call_permission_granted", isGranted).apply()
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("upperdot_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = !prefs.contains("call_permission_granted")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (isFirstLaunch || !hasPermission) {
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Call Permission Required", color = com.mail2dev.upperdot.ui.theme.PrimaryYellow) },
            text = { Text("UpperDot requires Call permission to enable swipe-to-call functionality for your connections.", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    permissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }) {
                    Text("Grant Permission", color = com.mail2dev.upperdot.ui.theme.AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Later", color = com.mail2dev.upperdot.ui.theme.TextSecondary)
                }
            },
            containerColor = com.mail2dev.upperdot.ui.theme.Surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as UpperDotApp

    val startRoute = remember {
        val prefs = context.getSharedPreferences("upperdot_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("isSetupComplete", false)) "auth_launchpad" else "onboarding"
    }
    
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    val prefs = context.getSharedPreferences("upperdot_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("isSetupComplete", true).apply()
                    navController.navigate("auth_launchpad") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("auth_launchpad") {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AuthViewModel(app.googleAuthService, app.syncManager)
                    }
                }
            )
            AuthLaunchpadScreen(
                onNavigateToDashboard = {
                    navController.navigate("connections_list") {
                        popUpTo("auth_launchpad") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
        composable(
            "connections_list?phone={phone}",
            deepLinks = listOf(navDeepLink { uriPattern = "upperdot://create_note?phone={phone}" })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone")
            val connectionsViewModel: ConnectionsListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ConnectionsListViewModel(
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository,
                            app.preferenceRepository
                        )
                    }
                }
            )
            
            ConnectionsListScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToContact = { contactId ->
                    navController.navigate("client_profile/$contactId")
                },
                onNavigateToAddContact = {
                    navController.navigate("add_contact?contactId=&phone=")
                },
                viewModel = connectionsViewModel,
                initialPhone = phone
            )
        }

        composable("add_contact?contactId={contactId}&phone={phone}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toLongOrNull()
            val phone = backStackEntry.arguments?.getString("phone")
            val addContactViewModel: AddContactViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory {
                    initializer {
                        AddContactViewModel(app.contactRepository, app.hierarchyRepository)
                    }
                }
            )

            LaunchedEffect(contactId, phone) {
                if (contactId != null) {
                    addContactViewModel.loadContact(contactId)
                } else if (!phone.isNullOrEmpty()) {
                    addContactViewModel.prefillPhoneNumber(phone)
                }
            }

            val currentStep by addContactViewModel.currentStep.collectAsState()
            
            when (currentStep) {
                0 -> AddContactCoreInfoScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStepSelected = { addContactViewModel.onStepSelected(it) },
                    viewModel = addContactViewModel
                )
                1 -> AddContactIdentityScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStepSelected = { addContactViewModel.onStepSelected(it) },
                    viewModel = addContactViewModel
                )
                2 -> AddContactCorporateScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStepSelected = { addContactViewModel.onStepSelected(it) },
                    viewModel = addContactViewModel
                )
                3 -> AddContactFinancialScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStepSelected = { addContactViewModel.onStepSelected(it) },
                    viewModel = addContactViewModel
                )
                else -> AddContactCoreInfoScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStepSelected = { addContactViewModel.onStepSelected(it) },
                    viewModel = addContactViewModel
                )
            }
        }
        
        composable("call_history") {
            val callHistoryViewModel: com.mail2dev.upperdot.ui.call_history.CallHistoryViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        com.mail2dev.upperdot.ui.call_history.CallHistoryViewModel(app.callLogRepository)
                    }
                }
            )
            CallHistoryScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDialer = {
                    navController.navigate("dialer")
                },
                onNavigateToAddContact = { phone ->
                    navController.navigate("add_contact?contactId=&phone=$phone")
                },
                viewModel = callHistoryViewModel
            )
        }

        composable("dialer") {
            DialerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("client_profile/{contactId}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toLongOrNull() ?: 0L
            val profileViewModel: ClientProfileDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ClientProfileDetailViewModel(
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository,
                            app.preferenceRepository
                        )
                    }
                }
            )
            ClientProfileDetailScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() },
                onEditContact = { id ->
                    // Navigation to edit mode
                    navController.navigate("add_contact?contactId=$id")
                },
                viewModel = profileViewModel
            )
        }
        
        composable("insights") {
            InsightsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            InsightsViewModel(
                                app.contactRepository,
                                app.noteRepository,
                                app.transactionRepository,
                                app.preferenceRepository
                            )
                        }
                    }
                )
            )
        }

        composable("my_profile") {
            val profileSettingsViewModel: ProfileSettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ProfileSettingsViewModel(
                            app.googleAuthService,
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository,
                            app.preferenceRepository,
                            app.applicationContext
                        )
                    }
                }
            )
            val walletViewModel: DigitalWalletViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DigitalWalletViewModel(app.bankCardRepository)
                    }
                }
            )
            MyProfileSettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSignOut = {
                    navController.navigate("auth_launchpad") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = profileSettingsViewModel,
                walletViewModel = walletViewModel
            )
        }

        composable("digital_wallet_management") {
            val walletViewModel: DigitalWalletViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DigitalWalletViewModel(app.bankCardRepository)
                    }
                }
            )
            DigitalWalletScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlans = { navController.navigate("plans") },
                viewModel = walletViewModel
            )
        }

        composable("manage_custom_groups") {
            val hierarchyViewModel: RelationshipHierarchyViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        RelationshipHierarchyViewModel(app.contactRepository, app.hierarchyRepository)
                    }
                }
            )
            RelationshipHierarchyScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hierarchyViewModel
            )
        }

        composable("advanced_app_settings") {
            val settingsViewModel: AdvancedSettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AdvancedSettingsViewModel(
                            app.contactRepository,
                            app.bankCardRepository,
                            app.syncManager,
                            app.preferenceRepository
                        )
                    }
                }
            )
            AdvancedSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }

        composable("data_vault_hub") {
            val dataVaultViewModel: DataVaultViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DataVaultViewModel(
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository,
                            app.bankCardRepository,
                            app.preferenceRepository,
                            app.googleDriveService,
                            app.applicationContext
                        )
                    }
                }
            )
            DataVaultManagementScreen(
                navController = navController,
                viewModel = dataVaultViewModel
            )
        }
    }
}
