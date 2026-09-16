package com.mail2dev.upperdot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mail2dev.upperdot.ui.add_contact.AddContactScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactViewModel
import com.mail2dev.upperdot.ui.app_settings.AdvancedSettingsScreen
import com.mail2dev.upperdot.ui.app_settings.AdvancedSettingsViewModel
import com.mail2dev.upperdot.ui.auth_launchpad.AuthLaunchpadScreen
import com.mail2dev.upperdot.ui.auth_launchpad.AuthState
import com.mail2dev.upperdot.ui.auth_launchpad.AuthViewModel
import com.mail2dev.upperdot.ui.call_history.CallHistoryScreen
import com.mail2dev.upperdot.ui.call_history.CallHistoryViewModel
import com.mail2dev.upperdot.ui.call_security.CallWhitelistScreen
import com.mail2dev.upperdot.ui.call_security.CallWhitelistViewModel
import com.mail2dev.upperdot.ui.connections_list.ConnectionsListScreen
import com.mail2dev.upperdot.ui.connections_list.ConnectionsListViewModel
import com.mail2dev.upperdot.ui.data_vault.DataVaultManagementScreen
import com.mail2dev.upperdot.ui.data_vault.DataVaultViewModel
import com.mail2dev.upperdot.ui.dialer.DialerScreen
import com.mail2dev.upperdot.ui.digital_wallet.DigitalWalletScreen
import com.mail2dev.upperdot.ui.digital_wallet.DigitalWalletViewModel
import com.mail2dev.upperdot.ui.insights.InsightsScreen
import com.mail2dev.upperdot.ui.insights.InsightsViewModel
import com.mail2dev.upperdot.ui.onboarding.OnboardingScreen
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailScreen
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailViewModel
import com.mail2dev.upperdot.ui.profile_settings.MyProfileSettingsScreen
import com.mail2dev.upperdot.ui.profile_settings.ProfileSettingsViewModel
import com.mail2dev.upperdot.ui.relationship_hierarchy.RelationshipHierarchyScreen
import com.mail2dev.upperdot.ui.relationship_hierarchy.RelationshipHierarchyViewModel
import com.mail2dev.upperdot.ui.theme.PrimaryYellow
import com.mail2dev.upperdot.ui.theme.UpperDotTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
class MainActivity : ComponentActivity() {
    private var internalNavController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UpperDotApp

        setContent {
            UpperDotTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    internalNavController = navController
                    val scope = rememberCoroutineScope()

                    // Shared ViewModels requiring custom factories
                    val authViewModel: AuthViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AuthViewModel(
                                    application = app,
                                    authService = app.googleAuthService,
                                    syncManager = app.syncManager,
                                    preferenceRepository = app.preferenceRepository
                                ) as T
                            }
                        }
                    )

                    val callHistoryViewModel: CallHistoryViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return CallHistoryViewModel(
                                    repository = app.callLogRepository,
                                    contactRepository = app.contactRepository,
                                    context = applicationContext
                                ) as T
                            }
                        }
                    )

                    val digitalWalletViewModel: DigitalWalletViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return DigitalWalletViewModel(
                                    repository = app.bankCardRepository,
                                    preferenceRepository = app.preferenceRepository
                                ) as T
                            }
                        }
                    )

                    val addContactViewModel: AddContactViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AddContactViewModel(
                                    repository = app.contactRepository,
                                    hierarchyRepository = app.hierarchyRepository,
                                    bankSuggestionRepository = app.bankSuggestionRepository,
                                    application = app
                                ) as T
                            }
                        }
                    )

                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            // 0. Route Dispatcher (Splash)
                            composable("splash") {
                                SplashDispatcher(
                                    authViewModel = authViewModel,
                                    preferenceRepository = app.preferenceRepository,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 1. Connections List (Dashboard)
                            composable("connections_list") {
                                val vm: ConnectionsListViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return ConnectionsListViewModel(
                                                repository = app.contactRepository,
                                                noteRepository = app.noteRepository,
                                                transactionRepository = app.transactionRepository,
                                                preferenceRepository = app.preferenceRepository
                                            ) as T
                                        }
                                    }
                                )
                                ConnectionsListScreen(
                                    onNavigate = { route -> navController.navigate(route) },
                                    onNavigateToContact = { id -> navController.navigate("profile_detail/$id") },
                                    onNavigateToAddContact = { 
                                        addContactViewModel.resetForm()
                                        navController.navigate("add_contact") 
                                    },
                                    viewModel = vm,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = this@composable
                                )
                            }

                            // 2. Client Profile Detail
                            composable(
                                route = "profile_detail/{contactId}",
                                arguments = listOf(navArgument("contactId") { type = NavType.LongType }),
                                deepLinks = listOf(navDeepLink { uriPattern = "upperdot://profile_detail/{contactId}" })
                            ) { backStackEntry ->
                                val contactId = backStackEntry.arguments?.getLong("contactId") ?: 0L
                                val vm: ClientProfileDetailViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return ClientProfileDetailViewModel(
                                                contactRepository = app.contactRepository,
                                                noteRepository = app.noteRepository,
                                                transactionRepository = app.transactionRepository,
                                                preferenceRepository = app.preferenceRepository
                                            ) as T
                                        }
                                    }
                                )
                                ClientProfileDetailScreen(
                                    contactId = contactId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onEditContact = { id -> navController.navigate("add_contact?editId=$id") },
                                    viewModel = vm,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = this@composable
                                )
                            }

                        // 3. Add Contact Wizard
                        composable(
                            route = "add_contact?editId={editId}&phone={phone}",
                            arguments = listOf(
                                navArgument("editId") { type = NavType.LongType; defaultValue = -1L },
                                navArgument("phone") { type = NavType.StringType; nullable = true; defaultValue = null }
                            ),
                            deepLinks = listOf(navDeepLink { uriPattern = "upperdot://add_contact?phone={phone}" })
                        ) { backStackEntry ->
                            val editId = backStackEntry.arguments?.getLong("editId") ?: -1L
                            val phone = backStackEntry.arguments?.getString("phone")

                            LaunchedEffect(phone) {
                                if (phone != null && editId <= 0L) {
                                    addContactViewModel.prefillPhoneNumber(phone)
                                }
                            }

                            AddContactScreen(
                                editId = editId,
                                onNavigateBack = { 
                                    navController.popBackStack("connections_list", inclusive = false) 
                                },
                                viewModel = addContactViewModel
                            )
                        }

                        // 4. Call History
                        composable(
                            route = "call_history",
                            deepLinks = listOf(navDeepLink { uriPattern = "upperdot://call_history" })
                        ) {
                            CallHistoryScreen(
                                onNavigate = { route -> navController.navigate(route) },
                                onNavigateToDialer = { navController.navigate("dialer") },
                                onNavigateToContact = { id -> navController.navigate("profile_detail/$id") },
                                onNavigateToAddContact = { phone: String -> 
                                    addContactViewModel.resetForm()
                                    navController.navigate("add_contact?phone=$phone") 
                                },
                                viewModel = callHistoryViewModel
                            )
                        }

                        // 5. Dialer
                        composable("dialer") {
                            DialerScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddContact = { phone: String -> 
                                    addContactViewModel.resetForm()
                                    navController.navigate("add_contact?phone=$phone")
                                },
                                onNavigateToContact = { id: Long -> 
                                    navController.navigate("profile_detail/$id") 
                                },
                                viewModel = callHistoryViewModel
                            )
                        }

                        // 6. Relationship Hierarchy
                        composable("relationship_hierarchy") {
                            val vm: RelationshipHierarchyViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return RelationshipHierarchyViewModel(
                                            contactRepository = app.contactRepository,
                                            hierarchyRepository = app.hierarchyRepository
                                        ) as T
                                    }
                                }
                            )
                            RelationshipHierarchyScreen(
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = vm
                            )
                        }

                            // 7. Data Vault Management
                            composable("data_vault") {
                                val vm: DataVaultViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return DataVaultViewModel(
                                                contactRepository = app.contactRepository,
                                                noteRepository = app.noteRepository,
                                                transactionRepository = app.transactionRepository,
                                                bankCardRepository = app.bankCardRepository,
                                                preferenceRepository = app.preferenceRepository,
                                                driveService = app.googleDriveService,
                                                context = applicationContext
                                            ) as T
                                        }
                                    }
                                )
                                DataVaultManagementScreen(
                                    navController = navController,
                                    viewModel = vm
                                )
                            }

                            // 8. Advanced Settings
                            composable("app_settings") {
                                val vm: AdvancedSettingsViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return AdvancedSettingsViewModel(
                                                contactRepository = app.contactRepository,
                                                bankCardRepository = app.bankCardRepository,
                                                noteRepository = app.noteRepository,
                                                transactionRepository = app.transactionRepository,
                                                syncManager = app.syncManager,
                                                preferenceRepository = app.preferenceRepository
                                            ) as T
                                        }
                                    }
                                )
                                AdvancedSettingsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigate = { route -> navController.navigate(route) },
                                    viewModel = vm
                                )
                            }

                            composable("call_whitelist") {
                                val vm: CallWhitelistViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return CallWhitelistViewModel(
                                                contactRepository = app.contactRepository
                                            ) as T
                                        }
                                    }
                                )
                                CallWhitelistScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = vm
                                )
                            }

                            // 9. Digital Wallet
                            composable("digital_wallet") {
                                DigitalWalletScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPlans = { navController.navigate("profile_settings") },
                                    viewModel = digitalWalletViewModel
                                )
                            }

                            // Add Alias for Digital Wallet Management to prevent crashes
                            composable("digital_wallet_management") {
                                DigitalWalletScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPlans = { navController.navigate("profile_settings") },
                                    viewModel = digitalWalletViewModel
                                )
                            }

                            // 10. Insights
                        composable("insights") {
                            val vm: InsightsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return InsightsViewModel(
                                            contactRepository = app.contactRepository,
                                            noteRepository = app.noteRepository,
                                            transactionRepository = app.transactionRepository,
                                            preferenceRepository = app.preferenceRepository,
                                            audioHandler = app.audioHandler
                                        ) as T
                                    }
                                }
                            )
                            InsightsScreen(
                                onNavigate = { route -> navController.navigate(route) },
                                viewModel = vm
                            )
                        }

                        // 11. Profile Settings
                        composable("my_profile") {
                            val vm: ProfileSettingsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return ProfileSettingsViewModel(
                                            authService = app.googleAuthService,
                                            contactRepository = app.contactRepository,
                                            noteRepository = app.noteRepository,
                                            transactionRepository = app.transactionRepository,
                                            preferenceRepository = app.preferenceRepository,
                                            syncManager = app.syncManager,
                                            context = applicationContext
                                        ) as T
                                    }
                                }
                            )
                            MyProfileSettingsScreen(
                                onNavigate = { route -> navController.navigate(route) },
                                onSignOut = { navController.navigate("auth_launchpad") },
                                viewModel = vm,
                                walletViewModel = digitalWalletViewModel
                            )
                        }

                        // 12. Auth Launchpad
                        composable("auth_launchpad") {
                            AuthLaunchpadScreen(
                                onNavigateToDashboard = {
                                    navController.navigate("splash") {
                                        popUpTo("auth_launchpad") { inclusive = true }
                                    }
                                },
                                viewModel = authViewModel
                            )
                        }

                        // 13. Onboarding
                        composable("onboarding") {
                            OnboardingScreen(
                                onComplete = {
                                    scope.launch {
                                        val current = app.preferenceRepository.preferences.first()
                                        app.preferenceRepository.savePreferences(current.copy(isOnboardingCompleted = true))
                                        navController.navigate("connections_list") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        internalNavController?.handleDeepLink(intent)
    }
}

@Composable
fun SplashDispatcher(
    authViewModel: AuthViewModel,
    preferenceRepository: com.mail2dev.upperdot.data.repository.PreferenceRepository,
    onNavigate: (String) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val preferences by preferenceRepository.preferences.collectAsState(initial = null)
    val context = LocalContext.current

    LaunchedEffect(authState, preferences) {
        if (preferences == null) return@LaunchedEffect

        when (authState) {
            is AuthState.Authenticated -> {
                val hasCorePermissions = checkCorePermissions(context)
                if (!hasCorePermissions || !preferences!!.isOnboardingCompleted) {
                    onNavigate("onboarding")
                } else {
                    // ALWAYS navigate to dashboard and pop splash.
                    // If a deep link is present, NavHost will handle the secondary navigation on top.
                    onNavigate("connections_list")
                }
            }
            is AuthState.Unauthenticated -> {
                onNavigate("auth_launchpad")
            }
            AuthState.Loading -> {
                // Wait for auth check
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryYellow)
    }
}

private fun checkCorePermissions(context: android.content.Context): Boolean {
    val permissions = arrayOf(
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS
    )
    return permissions.all {
        androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
