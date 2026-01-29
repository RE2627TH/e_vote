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
import androidx.compose.ui.text.style.TextAlign
import com.example.s_vote.ui.theme.*
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
                .padding(16.dp)
        ) {
            if (showResults) {
                Text(
                    "FINAL RESULTS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                )

                // ---------- HEADER ----------
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceLight)
                        .padding(12.dp)
                ) {
                    Text("Position", fontWeight = FontWeight.Black, color = TextPrimary, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall)
                    Text("Candidate", fontWeight = FontWeight.Black, color = TextPrimary, modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.labelSmall)
                    Text("Votes", fontWeight = FontWeight.Black, color = TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                    Text("Result", fontWeight = FontWeight.Black, color = TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Please wait for the admin to approve and publish the results.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
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

    // Winner Highlighting
    val winnerBrush = Brush.horizontalGradient(
        colors = listOf(
            Success.copy(alpha = 0.15f),
            Primary.copy(alpha = 0.1f)
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

    // Background: Surfaced slate for everyone, extra glow for winning
    val bgModifier = if (isWinner) {
        Modifier
            .background(winnerBrush, RoundedCornerShape(24.dp))
            .border(1.dp, Success, RoundedCornerShape(24.dp))
    } else {
        Modifier
            .background(SurfaceLight, RoundedCornerShape(24.dp))
            .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
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
                        text = result.position.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isWinner) Success else Primary,
                        letterSpacing = 1.sp
                    )

                    if (isWinner) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown), 
                            contentDescription = "Winner",
                            tint = Secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    result.candidate.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            // Votes
            Text(
                "$animatedVotes",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Percentage
            Text(
                "%.1f%%".format(animatedPercent),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = if (isWinner) Success else Primary
            )
        }

        Spacer(Modifier.height(10.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { animatedPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp)),
            trackColor = BackgroundLight,
            color = if (isWinner) Success else Primary
        )
    }
}