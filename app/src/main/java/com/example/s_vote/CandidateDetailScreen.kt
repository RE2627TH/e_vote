package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.CandidateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailScreen(navController: NavController, candidateId: String) {

    val viewModel: CandidateViewModel = viewModel()
    val candidates by viewModel.candidates.collectAsState()

    // Find specific candidate
    val candidate = candidates.find { it.id == candidateId }

    // If candidate unknown (e.g. deep link or reload), fetch again
    LaunchedEffect(Unit) {
        if (candidates.isEmpty()) {
            viewModel.fetchCandidates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Details") },
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
        }
    ) { padding ->

        if (candidate == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Profile Image with Gradient Border
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6A4CFF), Color(0xFFE04EF6))
                            )
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                        contentDescription = candidate.name ?: "Candidate Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = candidate.name ?: "Unknown Name",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = candidate.position ?: "No Position",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                 Text(
                    text = candidate.department ?: "N/A",
                    fontSize = 14.sp,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Cards
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Manifesto", fontWeight = FontWeight.Bold, color = Color(0xFF3E1F7F))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(candidate.manifesto ?: "No manifesto available.", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Feedback Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Student Feedback", fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (candidate.feedback.isNullOrEmpty()) {
                            Text("No feedback yet.", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            candidate.feedback.take(3).forEach { feedback ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(feedback.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("⭐ ${feedback.rating}", fontSize = 12.sp, color = Color(0xFFFFB300))
                                    }
                                    Text(feedback.comment, fontSize = 13.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Vote Button
                Button(
                    onClick = {
                        val safeId = candidate.id ?: ""
                        val safePosition = candidate.position ?: ""
                        if (safeId.isNotEmpty()) {
                            navController.navigate(
                                Routes.SCAN_ID
                                .replace("{candidateId}", safeId)
                                + "?position=$safePosition"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2104A1)
                    )
                ) {
                    Text("Vote for ${candidate.name ?: "Candidate"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
