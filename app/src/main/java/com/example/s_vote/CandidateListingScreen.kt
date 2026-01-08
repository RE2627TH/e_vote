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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListingScreen(navController: NavController, roleName: String) {

    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val candidates by viewModel.candidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCandidates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidates for $roleName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController, selectedRoute = Routes.POLL_HISTORY)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Filter candidates by role
                val filteredCandidates = candidates.filter {
                    val isApproved = it.status.equals("approved", ignoreCase = true)
                    val matchesRole = (it.position ?: "").lowercase().replace(" ", "_") == roleName.lowercase() ||
                            (it.role ?: "").lowercase() == roleName.lowercase()
                    isApproved && matchesRole
                }

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
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF6A4CFF).copy(alpha = 0.1f), Color(0xFF9370DB).copy(alpha = 0.1f))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Select the candidate you want to vote for",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    "Click on a candidate to view their manifesto and campaign details",
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "${filteredCandidates.size} Candidates Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(filteredCandidates) { candidate ->
                        CandidateCardEnhanced(
                            candidate = candidate,
                            onClick = {
                                val safeId = candidate.id ?: ""
                                if (safeId.isNotEmpty()) {
                                    navController.navigate(Routes.CANDIDATE_DETAIL.replace("{id}", safeId))
                                }
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
}

@Composable
fun CandidateCardEnhanced(candidate: com.example.s_vote.model.Candidate, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // LEFT: Role Icon in a Box
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6A4CFF).copy(alpha = 0.15f), Color(0xFF9370DB).copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(candidate.symbolResId ?: R.drawable.election_day),
                    contentDescription = candidate.position ?: "Position",
                    modifier = Modifier.size(40.dp)
                )
            }

            // CENTER: Candidate Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
                    color = Color(0xFF7A7A7A),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badges
                if (!candidate.badges.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        candidate.badges?.take(2)?.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEDE7FF))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "✓ $badge",
                                    fontSize = 9.sp,
                                    color = Color(0xFF6A4CFF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // RIGHT: Profile Photo
            Image(
                painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                contentDescription = candidate.name ?: "Candidate Photo",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
            )
        }
    }
}
