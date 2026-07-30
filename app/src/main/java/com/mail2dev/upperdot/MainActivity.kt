package com.mail2dev.upperdot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mail2dev.upperdot.ui.auth_launchpad.AuthLaunchpadScreen
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
            // TODO: Implement ConnectionsListScreen
        }
        composable("call_history") {
            // TODO: Implement CallHistoryScreen
        }
        composable("insights") {
            // TODO: Implement InsightsScreen
        }
        composable("my_profile") {
            // TODO: Implement MyProfileScreen
        }
    }
}
