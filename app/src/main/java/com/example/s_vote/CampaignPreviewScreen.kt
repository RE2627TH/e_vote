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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.api.ApiClient
import com.example.s_vote.viewmodel.CandidateViewModel
import com.example.s_vote.ui.theme.*
import com.example.s_vote.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignPreviewScreen(navController: NavController, candidateId: String) {

    val viewModel: CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()

    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Preview Campaign") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = profile!!
            val details = user.candidateDetails

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
                    .padding(innerPadding)
            ) {

                item {
                    // Banner Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Primary, BackgroundLight)
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = user.name.uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                (details?.position ?: "Candidate").uppercase(),
                                fontSize = 14.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                            .background(BackgroundLight)
                            .padding(20.dp)
                    ) {

                        // Profile Photo
                        val photoUrl = details?.photo?.let { path: String ->
                             if(path.startsWith("http")) path else "${ApiClient.BASE_URL}$path"
                        }

                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Candidate Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .background(SurfaceLight)
                                    .border(2.dp, Primary.copy(alpha = 0.1f), CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.candidates), // Fallback
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Symbol
                        val symbolUrl = details?.symbolUrl?.let { path: String ->
                             if(path.startsWith("http")) path else "${ApiClient.BASE_URL}$path"
                        }
                        
                        if (symbolUrl != null) {
                             Text(text = "My Symbol", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(8.dp))
                             AsyncImage(
                                model = symbolUrl,
                                contentDescription = "Symbol",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceLight)
                                    .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Leadership Focus
                        val tagline = details?.tagline
                        if (!tagline.isNullOrEmpty()) {
                            Text(
                                text = "Leadership Focus",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SurfaceLight)
                                    .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = tagline,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Campaign Goals
                        val goals: List<String> = details?.goals?.split("\n") ?: emptyList()
                        if (goals.isNotEmpty()) {
                            Text(
                                text = "Campaign Goals",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            goals.forEach { goal ->
                                if (goal.isNotBlank()) {
                                    GoalCard(goal)
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Pledges Section
                        val pledges: List<String> = details?.pledges?.split("\n") ?: emptyList()
                        if (pledges.isNotEmpty()) {
                             Text(
                                text = "My Pledges to You",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            pledges.forEach { pledge ->
                                if (pledge.isNotBlank()) {
                                    PledgeCard(pledge)
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // About Section (Manifesto)
                        val manifesto = details?.manifesto
                        if (!manifesto.isNullOrEmpty()) {
                            Text(
                                text = "About / Manifesto",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = manifesto,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Action Buttons
                        Button(
                            onClick = { /* TODO: Share campaign */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(Primary)
                        ) {
                            Text(text = "SHARE CAMPAIGN", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                        ) {
                            Text(text = "BACK", color = Primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight)
            .border(1.dp, SuccessMild.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "🎯",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 18.sp
            )
            Text(
                text = goal,
                fontSize = 14.sp,
                color = Success,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PledgeCard(pledge: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight)
            .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "💜",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 18.sp
            )
            Text(
                text = pledge,
                fontSize = 14.sp,
                color = Accent,
                lineHeight = 18.sp
            )
        }
    }
}