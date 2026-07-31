package com.mail2dev.upperdot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                RootNavigation()
            }
        }
    }
}

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as UpperDotApp
    
    NavHost(
        navController = navController,
        startDestination = "auth_launchpad",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("auth_launchpad") {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AuthViewModel(app.googleAuthService)
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
        composable("connections_list") {
            val connectionsViewModel: ConnectionsListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ConnectionsListViewModel(
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository
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
                    navController.navigate("add_contact")
                },
                viewModel = connectionsViewModel
            )
        }

        composable("add_contact") { backStackEntry ->
            val addContactViewModel: AddContactViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory {
                    initializer {
                        AddContactViewModel(app.contactRepository, app.hierarchyRepository)
                    }
                }
            )
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
            CallHistoryScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("client_profile/{contactId}") { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toLongOrNull() ?: 0L
            val profileViewModel: ClientProfileDetailViewModel = viewModel()
            ClientProfileDetailScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() },
                onEditContact = { id ->
                    // Navigation to edit mode
                    navController.navigate("add_contact")
                },
                viewModel = profileViewModel
            )
        }
        
        composable("insights") {
            val insightsViewModel: InsightsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        InsightsViewModel(
                            app.contactRepository,
                            app.noteRepository,
                            app.transactionRepository
                        )
                    }
                }
            )
            InsightsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = insightsViewModel
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
                            app.noteRepository,
                            app.transactionRepository,
                            app.bankCardRepository,
                            app.syncManager
                        )
                    }
                }
            )
            AdvancedSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }
    }
}
