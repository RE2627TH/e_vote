package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.model.Candidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollHistoryScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Vote History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {

                item {
                    // Info Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEDE7FF))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ℹ️", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(
                                    "Your voting records",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "Click to view candidate details",
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                items(candidateData) { candidate ->
                    PollHistoryCard(
                        candidate = candidate,
                        onClick = {
                            navController.navigate(Routes.CANDIDATE_DETAIL.replace("{id}", candidate.id ?: ""))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PollHistoryCard(candidate: Candidate, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Role icon container instead of candidate photo
            // Using role property, default to empty string if null
            val roleName = candidate.role ?: ""
            val roleIcon = when (roleName.lowercase()) {
                "president" -> R.drawable.president
                "vice president" -> R.drawable.election_day
                "sports secretary" -> R.drawable.election_results
                "cultural secretary" -> R.drawable.corporate_culture
                "discipline secretary" -> R.drawable.court
                "treasurer" -> R.drawable.candidates
                else -> R.drawable.sample_pic
            }

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDE7FF)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = roleIcon),
                    contentDescription = "role icon",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Candidate Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    candidate.name ?: "Unknown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A0033)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    candidate.position ?: "No Position",
                    fontSize = 13.sp,
                    color = Color(0xFF7A7A7A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badges
                if (!candidate.badges.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        candidate.badges?.take(2)?.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8DFF7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "✓ $badge",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6A4CFF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Arrow Icon
            Text(
                "→",
                fontSize = 20.sp,
                color = Color(0xFF6A4CFF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
