package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel()
    val stats by viewModel.dashboardStats.collectAsState()
    
    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "ADMIN DASHBOARD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundLight, SurfaceLight.copy(alpha = 0.5f))
                    )
                )
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ---------- LIVE STATISTICS GRID (2x2) ----------
            Text(
                "LIVE STATISTICS", 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatTile(
                    icon = Icons.Default.Groups, 
                    value = stats?.studentsCount ?: "0", 
                    label = "Students",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Default.People, 
                    value = stats?.candidatesCount ?: "0", 
                    label = "Candidates",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatTile(
                    icon = Icons.Default.HowToVote, 
                    value = stats?.activeElections ?: "0", 
                    label = "Active",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Default.Poll, 
                    value = stats?.votesCast ?: "0", 
                    label = "Votes",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- ADMINISTRATIVE ACTIONS ----------
            Text(
                "QUICK ACTIONS", 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionTile(
                    icon = Icons.Default.PersonSearch, 
                    title = "Verify Candidates", 
                    subtitle = "Approve or reject applications",
                    onClick = { navController.navigate(Routes.ADMIN_HOME) }
                )
                ActionTile(
                    icon = Icons.Default.PostAdd, 
                    title = "Create Election", 
                    subtitle = "Set up new ballot and roles",
                    onClick = { navController.navigate(Routes.CREATE_ELECTION) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionTileSmall(
                        icon = Icons.Default.Assessment, 
                        title = "Reports", 
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.REPORTS) }
                    )
                    ActionTileSmall(
                        icon = Icons.Default.BarChart, 
                        title = "Results", 
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.RESULT) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Footer
            OutlinedButton(
                onClick = { navController.navigate(Routes.LOGIN) { popUpTo(0) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Error)
                Spacer(Modifier.width(12.dp))
                Text("LOGOUT COMMAND", color = Error, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun StatTile(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 1.sp)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun ActionTileSmall(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 1.sp)
        }
    }
}
