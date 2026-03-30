package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.s_vote.ui.theme.*
import com.example.s_vote.candidateData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllFeedbackScreen(navController: NavController, candidateId: String) {

    val candidate = candidateData.find { it.id == candidateId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Feedback for ${candidate?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (candidate == null) {
            Column(
                modifier = Modifier.fillMaxSize().background(BackgroundLight)
                    .padding(padding),

            verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("Candidate not found")
            }
            return@Scaffold
        }

        if (candidate.feedback.isNullOrEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("No feedback yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(candidate.feedback ?: emptyList()) { fb ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(fb.userName, fontWeight = FontWeight.Black, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                (1..5).forEach { star ->
                                    Icon(
                                        painter = painterResource(if (fb.rating >= star) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(fb.comment, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
