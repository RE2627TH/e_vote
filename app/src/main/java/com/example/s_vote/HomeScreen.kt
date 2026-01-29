package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(navController: NavController) {

    val viewModel: com.example.s_vote.viewmodel.HomeViewModel = viewModel()
    val electionStatus by viewModel.electionStatus.collectAsState()
    val days by viewModel.countdownDays.collectAsState()
    val hours by viewModel.countdownHours.collectAsState()
    val minutes by viewModel.countdownMinutes.collectAsState()
    val seconds by viewModel.countdownSeconds.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BackgroundLight,
            SurfaceLight
        )
    )

    Scaffold(
        bottomBar = { BottomNavBar(navController, selectedRoute = Routes.HOME) },
        containerColor = BackgroundLight
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Aesthetic Glows
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = (-80).dp, x = (-80).dp)
                    .background(Primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // ---------- HEADER ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome Back! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.sp
                        )
                        Text(
                            "Let's shape the future today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Secondary.copy(alpha = 0.1f))
                            .border(
                                1.dp, 
                                Secondary.copy(alpha = 0.2f), 
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗳️", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ---------- COUNTDOWN ----------
                Text(
                    "Election Live Countdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Modern Countdown Card - Deep Professional
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            (electionStatus.title ?: "General Election").uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountdownBox(days, "DAYS")
                            CountdownBox(hours, "HOURS")
                            CountdownBox(minutes, "MINS")
                            CountdownBox(seconds, "SECS")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ---------- ROLE SELECTION ----------
                Text(
                    "Candidate Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                roleList.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { role ->
                            val safeRoleName = role.name.lowercase().replace(" ", "_")
                            Box(modifier = Modifier.weight(1f)) {
                                RoleCard(
                                    roleName = role.name,
                                    icon = role.icon,
                                    onClick = {
                                        navController.navigate(
                                            Routes.CANDIDATE_LIST.replace("{roleName}", safeRoleName)
                                        )
                                    }
                                )
                            }
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---------- RESOURCES ----------
                Text(
                    "Election Resources",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard("📋", "Manifestos", Modifier.weight(1f)) {
                        navController.navigate(Routes.MANIFESTO)
                    }
                    
                    InfoCard("❓", "FAQs", Modifier.weight(1f)) {
                        navController.navigate(Routes.FAQ)
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun RoleCard(roleName: String, icon: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLight)
            .border(
                1.dp,
                OutlineColor,
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = roleName,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                roleName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun InfoCard(emoji: String, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLight)
            .border(
                1.dp,
                OutlineColor,
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title.uppercase(), 
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CountdownBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BackgroundLight)
                .border(
                    1.dp,
                    OutlineColor,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = Primary,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}
