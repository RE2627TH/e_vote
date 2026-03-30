package com.example.s_vote

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.ResultViewModel
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController) {
    val viewModel: ResultViewModel = viewModel()
    val electionStatus by viewModel.electionStatus.collectAsState()
    val rawResults by viewModel.results.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchElectionStatus()
    }

    // Role-based visibility logic
    val isAdmin = userRole == "admin"
    val status = electionStatus?.status ?: "none"
    val isUpcoming = status == "UPCOMING"
    val isRunning = status == "ACTIVE"
    val isEnded = status == "CLOSED"
    val isPublished = electionStatus?.isPublished == true
    val isCancelled = status == "CANCELLED"
    
    // Improved No Election logic: if cancelled, or if it's "none", or if there's no data
    val isNoElection = isCancelled || status == "none" || electionStatus == null

    var showEndDialog by remember { mutableStateOf(false) }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End Election?", fontWeight = FontWeight.Bold) },
            text = { Text("This will stop all voting and publish the final results to all students and candidates. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endElection()
                        showEndDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("END ELECTION")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Process raw results into UI model with percentages and ranks
    val groupedResults = rawResults.groupBy { it.position }
    val uiResults = remember(rawResults) {
        val list = mutableListOf<UserElectionResult>()
        groupedResults.forEach { (position, results) ->
            val totalVotesForPos = results.sumOf { it.voteCount.toIntOrNull() ?: 0 }
            val sortedPosResults = results.sortedByDescending { it.voteCount.toIntOrNull() ?: 0 }
            
            sortedPosResults.forEachIndexed { index, result ->
                val votes = result.voteCount.toIntOrNull() ?: 0
                val percentage = if (totalVotesForPos > 0) (votes.toFloat() / totalVotesForPos) * 100 else 0f
                list.add(
                    UserElectionResult(
                        candidateId = result.userId,
                        position = position,
                        candidate = result.name,
                        candidateImage = R.drawable.ic_launcher_foreground, 
                        votes = votes,
                        percentage = percentage,
                        rank = index + 1
                    )
                )
            }
        }
        list.sortedBy { it.position } // Sort by position name for consistency
    }

    val winners = uiResults.filter { it.rank == 1 }

    Scaffold(
        bottomBar = { 
            if (userRole != "candidate" && userRole != "admin") {
                BottomNavBar(navController, selectedRoute = com.example.s_vote.navigation.Routes.RESULT) 
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "ELECTION RESULTS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        if (isAdmin) {
                            Text(
                                "LIVE UPDATES ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                   if (isAdmin && isRunning) {
                       Button(
                           onClick = { showEndDialog = true },
                           colors = ButtonDefaults.buttonColors(containerColor = Error),
                           contentPadding = PaddingValues(horizontal = 12.dp),
                           modifier = Modifier.padding(end = 8.dp)
                       ) {
                           Text("END ELECTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                       }
                   }
                   if (isAdmin) {
                       IconButton(onClick = { viewModel.fetchElectionStatus() }) {
                           Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
                       }
                   }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            when {
                isLoading && rawResults.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                isNoElection -> {
                    NoElectionContent()
                }
                isAdmin -> {
                    ResultListContent(uiResults, winners, isAdmin, isEnded, isRunning, navController)
                }
                isEnded && isPublished -> {
                    ResultListContent(uiResults, winners, isAdmin, isEnded, isRunning, navController)
                    
                    // Winner Celebration Confetti (kept from implementation)
                    KonfettiView(
                        modifier = Modifier.fillMaxSize(),
                        parties = listOf(
                            Party(
                                speed = 0f,
                                maxSpeed = 30f,
                                damping = 0.9f,
                                angle = 270,
                                spread = 360,
                                colors = listOf(Primary.toArgb(), Success.toArgb(), Warning.toArgb()),
                                position = Position.Relative(0.5, 0.3),
                                emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(50)
                            )
                        )
                    )
                }
                isRunning || isUpcoming || isEnded -> {
                    ResultsLockedContent(isUpcoming = isUpcoming, isEnded = isEnded)
                }
                else -> {
                    NoElectionContent()
                }
            }
        }
    }
}

@Composable
fun ResultListContent(
    uiResults: List<UserElectionResult>, 
    winners: List<UserElectionResult>,
    isAdmin: Boolean,
    isEnded: Boolean,
    isRunning: Boolean,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val label = when {
                isAdmin && isRunning -> "ADMIN: REAL-TIME LIVE DATA"
                isRunning -> "LIVE ELECTION UPDATES - REAL TIME"
                isEnded -> "OFFICIAL FINAL RANKINGS"
                else -> "ELECTION RESULTS"
            }
            
            if (isEnded && winners.isNotEmpty() && !isAdmin) {
                WinnerCelebrationHeader()
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (isRunning) Success else TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp, top = if (isEnded && !isAdmin) 16.dp else 0.dp)
            )
        }

        if (winners.isNotEmpty() && isEnded) {
            item {
                WinnerPodium(winners, navController)
            }
        }

        item {
            Text(
                text = "FULL LEADERBOARD",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(uiResults) { result ->
            AnimatedResultRow(result, navController)
        }
    }
}

@Composable
fun WinnerPodium(winners: List<UserElectionResult>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary.copy(alpha = 0.08f), Color.Transparent)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_crown), 
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "TOP CANDIDATES",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(winners) { winner ->
                WinnerCard(winner, navController)
            }
        }
    }
}

@Composable
fun WinnerCelebrationHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = Primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🏆", fontSize = 40.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "CONGRATULATIONS!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Primary,
            letterSpacing = 4.sp
        )
        
        Text(
            "The election has concluded.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun WinnerCard(winner: UserElectionResult, navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val borderColor by if (winner.rank == 1) {
        infiniteTransition.animateColor(
            initialValue = Warning.copy(alpha = 0.5f),
            targetValue = Warning,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "border"
        )
    } else {
        remember { mutableStateOf(OutlineColor) }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.8f) + fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(vertical = 16.dp)
                .graphicsLayer { translationY = floatOffset }
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (winner.rank == 1) Warning.copy(alpha = 0.5f) else Primary.copy(alpha = 0.2f)
                )
                .clickable {
                    navController.navigate(com.example.s_vote.navigation.Routes.candidateDetail(winner.candidateId))
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            border = BorderStroke(width = if (winner.rank == 1) 2.5.dp else 1.dp, color = borderColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(100.dp).shadow(10.dp, CircleShape),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Image(
                            painter = painterResource(id = winner.candidateImage),
                            contentDescription = null,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = if (winner.rank == 1) Warning else Primary,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("1", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    winner.candidate.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
                
                Text(
                    winner.position.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Success,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 WINNER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.3f)))
                        Spacer(Modifier.width(8.dp))
                        Text("${winner.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun NoElectionContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(OutlineColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🗳️", fontSize = 48.sp)
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "NO ACTIVE ELECTION",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            "There are no scheduled or ongoing elections at this moment. Please check back later for updates.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ResultsLockedContent(isUpcoming: Boolean = false, isEnded: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = when {
                isUpcoming -> Primary.copy(alpha = 0.1f)
                isEnded -> Success.copy(alpha = 0.1f)
                else -> Warning.copy(alpha = 0.1f)
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when {
                        isUpcoming -> Primary
                        isEnded -> Success
                        else -> Warning
                    }
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "RESULTS ARE LOCKED",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(12.dp))
        
        val message = when {
            isUpcoming -> "The election hasn't started yet. Results will be available once the election is completed and published by the administration."
            isEnded -> "Polling has concluded. Results are currently being finalized and verified by the administration. They will be published shortly."
            else -> "The election is currently in progress. Real-time results are restricted to ensure a fair voting process. Check back once the polls close."
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = when {
                isUpcoming -> Primary
                isEnded -> Success
                else -> Warning
            },
            trackColor = (when {
                isUpcoming -> Primary
                isEnded -> Success
                else -> Warning
            }).copy(alpha = 0.1f)
        )
        Text(
            text = when {
                isUpcoming -> "WAITING FOR START"
                isEnded -> "POLLING ENDED"
                else -> "ELECTION IN PROGRESS"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when {
                isUpcoming -> Primary
                isEnded -> Success
                else -> Warning
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun AnimatedResultRow(result: UserElectionResult, navController: NavController) {
    val isWinner = result.rank == 1

    val animatedPercent by animateFloatAsState(
        targetValue = result.percentage,
        animationSpec = tween(1000)
    )

    val animatedVotes by animateIntAsState(
        targetValue = result.votes,
        animationSpec = tween(1000)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
            .clickable {
                navController.navigate(com.example.s_vote.navigation.Routes.candidateDetail(result.candidateId))
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = if (isWinner) BorderStroke(2.dp, Success.copy(alpha = 0.5f)) else BorderStroke(1.dp, OutlineColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Badge
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = when(result.rank) {
                        1 -> Warning.copy(alpha = 0.1f)
                        2 -> Primary.copy(alpha = 0.1f)
                        3 -> Primary.copy(alpha = 0.05f)
                        else -> BackgroundLight
                    },
                    border = if (result.rank <= 3) BorderStroke(1.dp, when(result.rank) {
                        1 -> Warning
                        2 -> Primary
                        else -> Primary.copy(alpha = 0.5f)
                    }) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${result.rank}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = when(result.rank) {
                                1 -> Warning
                                2 -> Primary
                                else -> TextSecondary
                            }
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        result.position.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        result.candidate.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$animatedVotes VOTES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        "${animatedPercent.toInt()}% Share",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWinner) Success else TextSecondary
                    )
                }
            }

            if (isWinner) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "LEADING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Success
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (result.percentage > 50) Success else Primary,
                trackColor = BackgroundLight
            )
        }
    }
}

data class UserElectionResult(
    val candidateId: String,
    val position: String,
    val candidate: String,
    val candidateImage: Int,
    val votes: Int,
    val percentage: Float,
    val rank: Int
)
