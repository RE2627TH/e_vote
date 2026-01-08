package com.example.s_vote
import com.example.s_vote.navigation.Routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADMIN PANEL") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6743FF),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Person, contentDescription = "Person")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)


        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF3F0FF))
                    .padding(10.dp)
            ) {
                Text("MENU", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(6.dp))
                AdminMenuItem(icon = Icons.Default.Dashboard, text = "Dashboard", isSelected = true, onClick = { navController.navigate(Routes.ADMIN_HOME) })
                AdminMenuItem(icon = Icons.Default.People, text = "Manage Candidates", onClick = { navController.navigate(Routes.MANAGE_CANDIDATES) })
                AdminMenuItem(icon = Icons.Default.HowToVote, text = "Create Election", onClick = { navController.navigate(Routes.CREATE_ELECTION) })
                AdminMenuItem(icon = Icons.Default.Poll, text = "Results", onClick = { navController.navigate(Routes.RESULT) })
                AdminMenuItem(icon = Icons.Default.Assessment, text = "Reports", onClick = { navController.navigate(Routes.REPORTS) })
                AdminMenuItem(icon = Icons.Default.Logout, text = "Logout", onClick = { navController.navigate(Routes.LOGIN) { popUpTo(0) } })
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val totalCandidates = candidateData.size

                DashboardCard(icon = Icons.Default.Groups, value = "1,250", label = "Total Students")
                DashboardCard(icon = Icons.Default.People, value = totalCandidates.toString(), label = "Total Candidates")
                DashboardCard(icon = Icons.Default.HowToVote, value = "45", label = "Active Elections")
                DashboardCard(icon = Icons.Default.Poll, value = "890", label = "Votes Cast")
            }
        }
    }
}

@Composable
fun AdminMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Icon(icon, contentDescription = text, tint = if (isSelected) Color(0xFF6743FF) else Color.Gray)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = if (isSelected) Color(0xFF6743FF) else Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DashboardCard(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF6743FF), modifier = Modifier.size(40.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6743FF))
        Text(text = label, fontSize = 14.sp, color = Color.Black)
    }
}
