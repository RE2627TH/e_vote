package com.example.s_vote

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.viewmodel.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController) {
    val viewModel: ResultViewModel = viewModel()
    val electionStatus by viewModel.electionStatus.collectAsState()
    val rawResults by viewModel.results.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchElectionStatus()
    }

    // Only show results if election is ENDED AND published
    val showResults = electionStatus?.isActive == false && electionStatus?.isPublished == true

    // Process raw results into UI model with percentages
    // Reuse logic from AdminResultsScreen or similar
    // We can define a local UiElectionResult here or map directly
    val groupedResults = rawResults.groupBy { it.position }
    val uiResults = remember(rawResults) {
        rawResults.map { result ->
            val totalVotesForPos = groupedResults[result.position]?.sumOf { it.voteCount.toIntOrNull() ?: 0 } ?: 0
            val votes = result.voteCount.toIntOrNull() ?: 0
            val percentage = if (totalVotesForPos > 0) (votes.toFloat() / totalVotesForPos) * 100 else 0f
            
            UiElectionResult( // Reusing the data class if it's accessible. If not, I'll define a local one.
                // Wait, UiElectionResult was defined in AdminResultsScreen.kt. It might not be visible here.
                // I should define a local one or move it to models.
                // For safety, I'll define a local one here calling it UserElectionResult
                position = result.position,
                candidate = result.name,
                candidateImage = R.drawable.ic_launcher_foreground, // Placeholder
                votes = votes,
                percentage = percentage
            )
        }.sortedByDescending { it.percentage }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approve Results") }, // Just title
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
            if (showResults) {
                Text(
                    "FINAL RESULTS",
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
                    items(uiResults) { result ->
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
                        "Results Not Yet Published",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Please wait for the admin to approve and publish the results.",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Local UI model
data class UserElectionResult(
    val position: String,
    val candidate: String,
    val candidateImage: Int,
    val votes: Int,
    val percentage: Float
)

@Composable
fun AnimatedResultRow(result: UserElectionResult) {

    // WINNER highlight
    val isWinner = result.percentage > 50

    // Gold Gradient for Winner
    val winnerBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFD700),   // Gold
            Color(0xFFFFE680)    // Light Gold
        )
    )

    // Animate row entry
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800)
    )

    val animatedPercent by animateFloatAsState(
        targetValue = result.percentage,
        animationSpec = tween(1000)
    )

    val animatedVotes by animateIntAsState(
        targetValue = result.votes,
        animationSpec = tween(1000)
    )

    // Background: Gold for winner, soft lavender for others
    val bgModifier = if (isWinner) {
        Modifier
            .background(winnerBrush, RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFFFC300), RoundedCornerShape(16.dp))
            .shadow(12.dp, RoundedCornerShape(16.dp)) // GLow effect
    } else {
        Modifier
            .background(Color(0xFFF8F6FF), RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(bgModifier)
            .padding(14.dp)
            .alpha(alphaAnim)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            // Candidate Photo
            Image(
                painter = painterResource(id = result.candidateImage),
                contentDescription = null,
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1.8f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.position,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    if (isWinner) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown), // Add crown icon
                            contentDescription = "Winner",
                            tint = Color(0xFFEFB700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    result.candidate,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWinner) Color(0xFF7A5200) else Color(0xFF5A3AE8)
                )
            }

            // Votes
            Text(
                "$animatedVotes",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) Color(0xFF6F4F00) else Color.Black
            )

            // Percentage
            Text(
                "%.1f%%".format(animatedPercent),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) Color(0xFF2E7D32) else Color.Red
            )
        }

        Spacer(Modifier.height(10.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = animatedPercent / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp)),
            trackColor = Color(0xFFE0E0E0),
            color = if (isWinner) Color(0xFF8C6E00) else Color.Red
        )
    }
}