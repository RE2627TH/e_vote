package com.example.s_vote

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController) {
    val electionHasEnded = !currentElection.isActive // Show results when election is NOT active

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Election Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF18048A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(12.dp)
        ) {
            if (electionHasEnded) {
                Text(
                    "ELECTION RESULTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                // ---------- HEADER ----------
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEDE7FF), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Position", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("Candidate", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                    Text("Votes", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("Result", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(electionResults) { result ->
                        AnimatedResultRow(result)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🗳️",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "Election In Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Results will be published after the election has ended.",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}