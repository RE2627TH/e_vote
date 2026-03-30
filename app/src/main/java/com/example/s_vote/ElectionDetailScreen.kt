package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectionDetailScreen(navController: NavController, electionId: String) {
    val viewModel: HomeViewModel = viewModel()
    val allElections by viewModel.allElections.collectAsState()
    val election = allElections.find { it.id == electionId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ELECTION DETAILS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (election == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    election.title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Status Badge
                val statusColor = when (election.status) {
                    "ACTIVE" -> Success
                    "UPCOMING" -> Primary
                    else -> TextMuted
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        when(election.status) {
                            "UPCOMING" -> "GOING TO START"
                            "ACTIVE" -> "LIVE NOW"
                            else -> "ELECTION ENDED"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Election Info Cards
                InfoDetailCard(
                    icon = Icons.Default.Event,
                    title = "Start Schedule",
                    content = election.startDate ?: "TBD"
                )
                
                Spacer(Modifier.height(16.dp))

                InfoDetailCard(
                    icon = Icons.Default.Event,
                    title = "End Schedule",
                    content = election.endDate ?: "TBD"
                )

                Spacer(Modifier.height(32.dp))

                // Status Message & Button
                if (election.status == "UPCOMING") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Voting will be enabled once the election starts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = { 
                        if (election.status == "ACTIVE") {
                             navController.navigate(Routes.CANDIDATE_LIST.replace("{roleName}", election.title.lowercase().replace(" ", "_")))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (election.status == "ACTIVE") Success else TextMuted
                    ),
                    enabled = election.status == "ACTIVE"
                ) {
                    Text(
                        if (election.status == "ACTIVE") "VOTE NOW" else "VOTING DISABLED",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                
                if (election.status == "CLOSED") {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Election has ended. Thank you for participating.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InfoDetailCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(content, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
