package com.example.s_vote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes

// ------------------- BOTTOM NAV BAR (Shared across all screens) -------------------
@Composable
fun BottomNavBar(navController: NavController, selectedRoute: String) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.shadow(elevation = 8.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = selectedRoute == Routes.HOME,
            onClick = { navController.navigate(Routes.HOME) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Black.copy(alpha = 0.6f),
                unselectedTextColor = Color.Black.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Poll, contentDescription = null) },
            label = { Text("Result") },
            selected = selectedRoute == Routes.RESULT,
            onClick = { navController.navigate(Routes.RESULT) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Black.copy(alpha = 0.6f),
                unselectedTextColor = Color.Black.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("History") },
            selected = selectedRoute == Routes.POLL_HISTORY,
            onClick = { navController.navigate(Routes.POLL_HISTORY) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Black.copy(alpha = 0.6f),
                unselectedTextColor = Color.Black.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = selectedRoute == Routes.PROFILE,
            onClick = { navController.navigate(Routes.PROFILE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = Color.Black.copy(alpha = 0.6f),
                unselectedTextColor = Color.Black.copy(alpha = 0.6f)
            )
        )
    }
}
