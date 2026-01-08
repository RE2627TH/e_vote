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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(navController: NavController) {

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

            val votedPercentage = 0.8f
            DonutChart(votedPercentage = votedPercentage)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${(votedPercentage * 100).toInt()}%", fontSize = 20.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(24.dp))

            BarChart()
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
fun BarChart() {
    val data = listOf(20f, 100f, 60f)
    val labels = listOf("Candidate A", "Candidate B", "Candidate C")
    val max = (data.maxOrNull() ?: 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEachIndexed { idx, value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .height(((value / max) * 140f).dp)
                        .width(40.dp)
                        .background(if (idx == 1) Color(0xFF4A90E2) else Color(0xFF9AD3B7))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(labels[idx], fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
