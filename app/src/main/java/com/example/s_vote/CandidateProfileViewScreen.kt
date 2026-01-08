package com.example.s_vote

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.CandidateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateProfileViewScreen(navController: NavController, candidateId: String) {

    val viewModel: CandidateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Candidate not found", color = Color.Gray)
            }
        } else {
            val user = profile!!
            val details = user.candidateDetails

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF6F6F8))
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp) // Space for button
                ) {
                    // --- HEADER ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        // Gradient Background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF2104A1), Color(0xFF6743FF))
                                    )
                                )
                        )
                        
                        // Profile Info
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             AsyncImage(
                                model = details?.photo ?: R.drawable.candidates, // Fallback fixed
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                user.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                details?.position ?: "Candidate",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // --- DETAILS ---
                    Column(Modifier.padding(16.dp)) {

                        // Manifesto
                        CardSection(title = "Manifesto 📜") {
                             Text(
                                details?.manifesto ?: "No manifesto provided.",
                                lineHeight = 20.sp,
                                color = Color(0xFF333333)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Goals
                        CardSection(title = "Campaign Goals 🎯") {
                             Text(
                                details?.goals ?: "No specific goals listed.",
                                lineHeight = 20.sp,
                                color = Color(0xFF333333)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        // Pledges
                         CardSection(title = "My Pledge 🤝") {
                             Text(
                                details?.pledges ?: "I pledge to serve with integrity.",
                                lineHeight = 20.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color(0xFF333333)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))

                        // Symbol
                         CardSection(title = "Election Symbol") {
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                  AsyncImage(
                                    model = details?.symbolUrl ?: R.drawable.ic_launcher_foreground,
                                    contentDescription = "Symbol",
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text("This is my symbol on the ballot.")
                             }
                        }
                    }
                }

                // --- VOTE BUTTON ---
                Button(
                    onClick = {
                        val pos = details?.position ?: ""
                        // Basic URL encoding for position
                        val encodedPos = java.net.URLEncoder.encode(pos, "UTF-8")
                        navController.navigate("scan_id/$candidateId?position=$encodedPos")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VOTE FOR ${user.name.uppercase()}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CardSection(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2104A1))
             Spacer(Modifier.height(8.dp))
             content()
        }
    }
}