package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object CallLogs : BottomNavItem("call_history", "Call Logs", Icons.Filled.Call, Icons.Outlined.Call)
    object Contact : BottomNavItem("connections_list", "Contact", Icons.Filled.Person, Icons.Outlined.Person)
    object Insight : BottomNavItem("insights", "Insight", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object MyProfile : BottomNavItem("my_profile", "My Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
}

@Composable
fun UpperDotBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.CallLogs,
        BottomNavItem.Contact,
        BottomNavItem.Insight,
        BottomNavItem.MyProfile
    )

    NavigationBar(
        containerColor = Color.Black,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.Black else Color.White
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        color = if (isSelected) AccentCyan else Color.White
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = AccentCyan,
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.White,
                    selectedTextColor = AccentCyan,
                    unselectedTextColor = Color.White
                )
            )
        }
    }
}
