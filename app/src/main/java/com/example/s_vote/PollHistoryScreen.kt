package com.example.s_vote

import androidx.compose.foundation.border

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.model.Candidate
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollHistoryScreen(navController: NavController) {

    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

    val viewModel: com.example.s_vote.viewmodel.VoteHistoryViewModel = viewModel()
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchVoteHistory(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Vote History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundLight)
        ) {

            when (val state = historyState) {
                is com.example.s_vote.viewmodel.VoteHistoryState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2104A1))
                    }
                }
                is com.example.s_vote.viewmodel.VoteHistoryState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = TextSecondary)
                    }
                }
                is com.example.s_vote.viewmodel.VoteHistoryState.Success -> {
                    val historyItems = state.theHistory
                    
                    if (historyItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No votes cast yet", fontWeight = FontWeight.Black, color = TextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("Your voting records will appear here.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                        ) {
                            item {
                                // Info Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Primary.copy(alpha = 0.1f))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ℹ️", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                                        Column {
                                            Text(
                                                "Your voting records",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = TextPrimary,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                "Summary of your participation",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            items(items = historyItems) { item: com.example.s_vote.model.VoteHistoryItem ->
                                PollHistoryCard(
                                    item = item,
                                    onClick = {
                                        // Optional: Navigate to candidate details or just show info
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PollHistoryCard(item: com.example.s_vote.model.VoteHistoryItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLight)
            .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Role icon container
            val position = item.position.lowercase()
            val roleIcon = when {
                position.contains("president") -> R.drawable.president
                position.contains("sports") -> R.drawable.election_day
                position.contains("cultural") -> R.drawable.corporate_culture
                position.contains("discipline") -> R.drawable.court
                position.contains("treasurer") -> R.drawable.candidates
                else -> R.drawable.sample_pic
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = roleIcon),
                    contentDescription = "role icon",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.candidateName.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    item.position.uppercase(),
                    fontSize = 12.sp,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    item.electionTitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "VOTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessMild,
                    letterSpacing = 1.sp
                )
                
                val date = item.timestamp.split(" ")[0]
                Text(
                    date,
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}
