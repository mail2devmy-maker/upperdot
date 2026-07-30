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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mail2dev.upperdot.ui.add_contact.AddContactCoreInfoScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactCorporateScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactFinancialScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactIdentityScreen
import com.mail2dev.upperdot.ui.add_contact.AddContactViewModel
import com.mail2dev.upperdot.ui.auth_launchpad.AuthLaunchpadScreen
import com.mail2dev.upperdot.ui.call_history.CallHistoryScreen
import com.mail2dev.upperdot.ui.connections_list.ConnectionsListScreen
import com.mail2dev.upperdot.ui.insights.InsightsScreen
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailScreen
import com.mail2dev.upperdot.ui.profile_detail.ClientProfileDetailViewModel
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
    NavHost(
        navController = navController,
        startDestination = "auth_launchpad",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("auth_launchpad") {
            AuthLaunchpadScreen(
                onNavigateToDashboard = {
                    navController.navigate("connections_list") {
                        popUpTo("auth_launchpad") { inclusive = true }
                    }
                }
            )
        }
        composable("connections_list") {
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
                }
            )
        }

        composable("add_contact") {
            val addContactViewModel: AddContactViewModel = viewModel()
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
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
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
            InsightsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable("my_profile") {
            // TODO: Implement MyProfileScreen
        }
    }
}
