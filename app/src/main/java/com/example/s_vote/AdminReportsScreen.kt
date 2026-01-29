package com.example.s_vote

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s_vote.viewmodel.AdminViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel()
    val stats by viewModel.dashboardStats.collectAsState()
    val results by viewModel.results.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose { viewModel.stopPolling() }
    }
    
    // Calculate Voted Percentage
    // Calculate Voted Percentage
    // Calculate Voted Percentage
    val totalStudents = stats?.studentsCount?.toIntOrNull()?.takeIf { it > 0 } ?: 1
    val votesCast = stats?.votesCast?.toIntOrNull() ?: 0
    val votedPercentage = (votesCast.toFloat() / totalStudents.toFloat()).coerceIn(0f, 1f)

    // Prepare Dynamic Bar Chart Data
    val groupedResults = results.groupBy { it.name }
    // Sum votes per candidate (in case of multiple positions, though usually one per candidate)
    val candidateVotes = groupedResults.map { (name, resList) ->
        name to resList.sumOf { it.voteCount.toIntOrNull() ?: 0 }
    }.sortedByDescending { it.second }

    val barData = candidateVotes.map { it.second.toFloat() }
    val barLabels = candidateVotes.map { it.first }
    
    // Assign colors
    val chartColors = listOf(
        Primary, Secondary, Success, 
        Color(0xFF6366F1), Color(0xFF818CF8), Color(0xFFA5B4FC),
        Color(0xFFC7D2FE), Color(0xFFE0E7FF)
    )

    Scaffold(
        containerColor = BackgroundLight, 
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "REPORTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
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
                .background(BackgroundLight)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "LIVE VOTING STATUS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(20.dp))

            DonutChart(votedPercentage = votedPercentage)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "${(votedPercentage * 100).toInt()}% VOTED", 
                style = MaterialTheme.typography.headlineMedium, 
                color = Success, 
                fontWeight = FontWeight.Black
            )
            Text(
                text = "$votesCast / $totalStudents STUDENTS", 
                style = MaterialTheme.typography.labelSmall, 
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("CANDIDATE PERFORMANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (barData.isNotEmpty()) {
                BarChart(data = barData, labels = barLabels, colors = chartColors.map { it.copy(alpha = 0.8f) })
            } else {
                 Text("No voting data available yet.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}


@Composable
fun DonutChart(votedPercentage: Float, size: Dp = 160.dp) {
    var played by remember { mutableStateOf(false) }
    val sweep by animateFloatAsState(
        targetValue = if (played) 360f * votedPercentage else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(Unit) { played = true }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawArc(
                color = SurfaceLight,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.14f)
            )
            drawArc(
                color = Success,
                startAngle = -90f,
                sweepAngle = sweep * 0.5f, // two-colored look (green part)
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.14f)
            )
            drawArc(
                color = Primary,
                startAngle = -90f + sweep * 0.5f,
                sweepAngle = sweep * 0.5f,
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.14f)
            )
        }
    }
}

@Composable
fun BarChart(data: List<Float>, labels: List<String>, colors: List<Color>) {
    val max = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Increased height for labels
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEachIndexed { idx, value ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .height(((value / max) * 150f).dp) // Scale height
                        .fillMaxWidth(0.6f)
                        .background(colors[idx % colors.size], RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Label (Candidate Name)
                Text(
                    text = labels.getOrElse(idx) { "" }.uppercase(),
                    fontSize = 10.sp, 
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                // Value
                Text(
                    text = "${value.toInt()}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
