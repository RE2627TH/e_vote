package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(navController: NavController) {

    val viewModel: com.example.s_vote.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val electionStatus by viewModel.electionStatus.collectAsState()
    val days by viewModel.countdownDays.collectAsState()
    val hours by viewModel.countdownHours.collectAsState()
    val minutes by viewModel.countdownMinutes.collectAsState()
    val seconds by viewModel.countdownSeconds.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController, selectedRoute = Routes.HOME) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F7FF), Color(0xFFEFE6FF))
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {

                // ---------- HEADER ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome Back! 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black

                        )
                        Text(
                            "Make your vote count today",
                            fontSize = 13.sp,
                            color = Color.Black
                                .copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF6A4CFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗳️", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // ---------- COUNTDOWN ----------
                
                // Controlled by ViewModel now

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFC9A7E6).copy(alpha = 0.8f),
                                    Color(0xFFD8BBDC).copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Countdown",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            electionStatus.title ?: "Election",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(1.dp))

                        // Time boxes row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountdownBox(days, "days")
                            CountdownBox(hours, "hours")
                            CountdownBox(minutes, "mins")
                            CountdownBox(seconds, "sec")
                        }

                        Spacer(modifier = Modifier.height(15.dp))



                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                // ---------- ROLE SELECTION ----------
                Text(
                    "Select Position to Vote",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black

                )

                Spacer(modifier = Modifier.height(3.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    items(roleList) { role ->

                        // 🔥 IMPORTANT FIX: normalize role name
                        val safeRoleName =
                            role.name.lowercase().replace(" ", "_")

                        RoleCard(
                            roleName = role.name,
                            icon = role.icon,
                            onClick = {
                                navController.navigate(
                                    Routes.CANDIDATE_LIST.replace(
                                        "{roleName}",
                                        safeRoleName
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ---------- LEARN MORE ----------
                Text(
                    "Learn More",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black

                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    item {
                        InfoCard("📋", "Manifestos") {
                            navController.navigate(Routes.MANIFESTO)
                        }
                    }

                    item {
                        InfoCard("❓", "FAQs") {
                            navController.navigate(Routes.FAQ)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RoleCard(roleName: String, icon: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .shadow(4.dp)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(icon),
                contentDescription = roleName,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                roleName.replace(" ", "\n"),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6A4CFF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InfoCard(emoji: String, title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE04EF6), Color(0xFFB79AFE))
                )
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CountdownBox(value: String, label: String) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8B6BB9).copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
