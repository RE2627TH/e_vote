package com.example.s_vote

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.s_vote.model.ElectionResult
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResultsScreen(navController: NavController, electionId: String) {
    val viewModel: AdminViewModel = viewModel()
    val rawResults by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(electionId) {
        viewModel.fetchResults(electionId)
    }

    // Process and Group Results by Position
    val groupedByPosition = remember(rawResults) {
        rawResults.groupBy { it.position }.mapValues { entry ->
            val totalVotesForPos = entry.value.sumOf { it.voteCount.toIntOrNull() ?: 0 }
            entry.value.map { result ->
                val votes = result.voteCount.toIntOrNull() ?: 0
                val percentage = if (totalVotesForPos > 0) (votes.toFloat() / totalVotesForPos) * 100f else 0f
                result to percentage
            }.sortedByDescending { it.first.voteCount.toIntOrNull() ?: 0 }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "ELECTION RESULTS", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchResults(electionId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Primary.copy(alpha = 0.05f),
                tonalElevation = 0.dp
            ) {
                Button(
                    onClick = { viewModel.publishResults() },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "APPROVE & PUBLISH",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading && rawResults.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Live Indicator
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Success.copy(alpha = alpha))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LIVE ELECTION UPDATES - REAL TIME",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Success,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // RESULTS BY POSITION
                groupedByPosition.forEach { (position, results) ->
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            position.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Primary,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    
                    itemsIndexed(results) { index, (result, percentage) ->
                        LeaderboardCard(index + 1, result, percentage)
                    }
                }
            }
        }
    }
}



@Composable
fun LeaderboardCard(rank: Int, result: ElectionResult, percentage: Float) {
    val isLeading = rank == 1
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, if (isLeading) Success.copy(alpha = 0.5f) else OutlineColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isLeading) Success.copy(alpha = 0.3f) else OutlineColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#$rank",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLeading) Success else TextSecondary
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(
                        result.position.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        result.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${result.voteCount} VOTES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        "${percentage.toInt()}% Share",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLeading) Success else TextSecondary
                    )
                }
            }
            
            if (isLeading) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Success, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "LEADING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Success,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Modern Progress Bar
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isLeading) Success else Primary,
                trackColor = BackgroundLight
            )
        }
    }
}
