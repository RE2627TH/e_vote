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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
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
                    .background(Color.White)
                    .padding(innerPadding)
            ) {

                item {
                    // Banner Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color(0xFF009688), Color(0xFF00796B))
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
                                text = user.name,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                details?.position ?: "Candidate",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                            .background(Color.White)
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
                                    .background(Color.LightGray)
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = tagline,
                                    fontSize = 14.sp,
                                    color = Color.Black.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
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
                                    color = Color.Black.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Action Buttons
                        Button(
                            onClick = { /* TODO: Share campaign */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFF009688))
                        ) {
                            Text(text = "Share Campaign", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(text = "Back", color = Color(0xFF009688))
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0F2F1))
            .padding(12.dp)
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
                color = Color(0xFF00796B),
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFCE4EC))
            .padding(12.dp)
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
                color = Color(0xFFC2185B),
                lineHeight = 18.sp
            )
        }
    }
}