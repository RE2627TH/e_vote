package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateProfileScreen(navController: NavController, candidateId: String) {

    // Using first mock candidate for preview if not found (assuming candidateData uses new model)
    // Note: In real app this should come from ViewModel, but we adapt existing code.
    val candidate = candidateData.firstOrNull { it.id == candidateId } ?: candidateData.firstOrNull() ?: Candidate(name="N/A")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Candidate Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {

            item {
                // Profile Header - Card Look
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                                .border(2.dp, Primary.copy(alpha = 0.1f), CircleShape)
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            candidate.name ?: "Unknown Name",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Text(
                            (candidate.position ?: "No Position").uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Personal Details Section
                Text("PERSONAL DETAILS", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        DetailRow("Course", candidate.course ?: "N/A")
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = OutlineColor.copy(alpha = 0.2f))
                        DetailRow("College", candidate.college ?: "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // About Section (Manifesto)
                Text("MANIFESTO", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        candidate.manifesto ?: "No manifesto available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Badges Section
                if (!candidate.badges.isNullOrEmpty()) {
                    Text("BADGES", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
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
                                        badge.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (index < candidate.badges.size - 1) {
                                    HorizontalDivider(Modifier.padding(vertical = 4.dp), color = OutlineColor.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                // Campaign Overview
                Text("CAMPAIGN OVERVIEW", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("LEADERSHIP FOCUS", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(
                            candidate.tagline ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("CAMPAIGN GOALS", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        val goalsList = candidate.goals?.split("\n") ?: emptyList()
                        if (goalsList.isNotEmpty()) {
                            goalsList.forEach { goal ->
                                if (goal.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("✓", color = Primary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(goal, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        } else {
                             Text("No goals listed.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Pledges
                Text("MY PLEDGES", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
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
                                        Text("💜", modifier = Modifier.padding(end = 12.dp))
                                        Text(pledge, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                         } else {
                             Text("No pledges listed.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                         }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Edit & Publish Buttons
                Button(
                    onClick = { /* TODO: Edit profile */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(Primary)
                ) {
                    Text("EDIT PROFILE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO: Publish changes */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(SuccessMild)
                ) {
                    Text("PUBLISH CHANGES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(48.dp))
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