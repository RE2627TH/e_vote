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
        Color(0xFF6743FF), Color(0xFF2104A1), Color(0xFFFFC300), 
        Color(0xFF4A90E2), Color(0xFF9AD3B7), Color(0xFFFF5722),
        Color(0xFFE91E63), Color(0xFF009688)
    )

    Scaffold(
        containerColor = Color.White, // ✅ WHITE BACKGROUND
        topBar = {
            TopAppBar(
                title = { Text("REPORT") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "LIVE VOTING STATUS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(20.dp))

            DonutChart(votedPercentage = votedPercentage)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${(votedPercentage * 100).toInt()}% Voted", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            Text(text = "$votesCast / $totalStudents students", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Candidate Performance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            if (barData.isNotEmpty()) {
                BarChart(data = barData, labels = barLabels, colors = chartColors)
            } else {
                 Text("No voting data available yet.", color = Color.Gray)
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
                color = Color(0xFFE6E6E6),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.14f)
            )
            drawArc(
                color = Color(0xFF9AD3B7),
                startAngle = -90f,
                sweepAngle = sweep * 0.5f, // two-colored look (green part)
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.14f)
            )
            drawArc(
                color = Color(0xFF4A90E2),
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
                    text = labels.getOrElse(idx) { "" },
                    fontSize = 10.sp, 
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                // Value
                Text(
                    text = "${value.toInt()}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
