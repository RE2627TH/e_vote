package com.example.s_vote.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { navController.navigate(Routes.HOME) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.RESULT,
            onClick = { navController.navigate(Routes.RESULT) },
            icon = { Icon(Icons.Default.Poll, null) },
            label = { Text("Result") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.POLL_HISTORY,
            onClick = { navController.navigate(Routes.POLL_HISTORY) },
            icon = { Icon(Icons.Default.History, null) },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { navController.navigate(Routes.PROFILE) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}