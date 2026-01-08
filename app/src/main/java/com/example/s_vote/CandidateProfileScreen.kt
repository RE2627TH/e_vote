package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.model.Candidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateProfileScreen(navController: NavController, candidateId: String) {

    // Using first mock candidate for preview if not found (assuming candidateData uses new model)
    // Note: In real app this should come from ViewModel, but we adapt existing code.
    val candidate = candidateData.firstOrNull { it.id == candidateId } ?: candidateData.firstOrNull() ?: Candidate(name="N/A")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Profile", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            item {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            candidate.name ?: "Unknown Name",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            candidate.position ?: "No Position",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email removed from model, so we skip or placeholder
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Personal Details Section
                Text("Personal Details", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column {
                        DetailRow("Course", candidate.course ?: "N/A")
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        DetailRow("College", candidate.college ?: "N/A")
                        // Email row removed
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // About Section (Manifesto)
                Text("About / Manifesto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        candidate.manifesto ?: "No manifesto available.",
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Badges Section
                if (!candidate.badges.isNullOrEmpty()) {
                    Text("Badges", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            candidate.badges.forEachIndexed { index, badge ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⭐", modifier = Modifier.padding(end = 12.dp))
                                    Text(
                                        badge,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (index < candidate.badges.size - 1) {
                                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item {
                // Campaign Overview
                Text("Campaign Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Leadership Focus", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        Text(
                            candidate.tagline ?: "N/A",
                            fontSize = 13.sp,
                            color = Color.Black,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Campaign Goals", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        val goalsList = candidate.goals?.split("\n") ?: emptyList()
                        if (goalsList.isNotEmpty()) {
                            goalsList.forEach { goal ->
                                if (goal.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(goal, fontSize = 13.sp, color = Color.Black)
                                    }
                                }
                            }
                        } else {
                             Text("No goals listed.", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Pledges
                Text("My Pledges", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEDE7FF))
                        .padding(16.dp)
                ) {
                    Column {
                        val pledgesList = candidate.pledges?.split("\n") ?: emptyList()
                         if (pledgesList.isNotEmpty()) {
                            pledgesList.forEach { pledge ->
                                if (pledge.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("💜", modifier = Modifier.padding(end = 8.dp))
                                        Text(pledge, fontSize = 13.sp, color = Color.Black)
                                    }
                                }
                            }
                         } else {
                             Text("No pledges listed.", fontSize = 13.sp, color = Color.Gray)
                         }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Edit & Publish Buttons
                Button(
                    onClick = { /* TODO: Edit profile */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF6A4BC2))
                ) {
                    Text("Edit Profile", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { /* TODO: Publish changes */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF2E7D32))
                ) {
                    Text("Publish Changes", color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            value ?: "N/A",
            fontSize = 13.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.65f)
        )
    }
}