package com.example.s_vote

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s_vote.viewmodel.CandidateViewModel
import com.example.s_vote.model.Candidate
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateFeedbackScreen(navController: NavController, candidateId: String) {
    val viewModel: CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
    
    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    val feedbackList = remember(profile) {
        profile?.candidateDetails?.feedback?.map { 
            FeedbackItem(
                id = it.userName + it.comment.hashCode(), // Generate pseudo-unique ID
                isAnonymous = false,
                name = it.userName,
                initials = it.userName.take(1).uppercase(),
                profileImage = null,
                rating = it.rating.toInt(),
                major = "Student", // Default
                timeAgo = "Recently", // Default
                isNew = false,
                comment = it.comment,
                year = "",
                showReply = false
            )
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Feedback") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
        ) {
            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Total",
                                tint = Color(0xFF2D0981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TOTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${feedbackList.size}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "All time received",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // New Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AVG RATING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f", if (feedbackList.isNotEmpty()) feedbackList.map { it.rating }.average() else 0.0),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Based on ${feedbackList.size} reviews",
                            fontSize = 10.sp,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Filter Chips
            var selectedFilter by remember { mutableStateOf("All Feedback") }
            val filters = listOf("All Feedback", "Recent", "Anonymous", "Suggestions")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceLight,
                            labelColor = TextPrimary,
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Feedback List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feedbackList) { feedback ->
                    FeedbackCard(feedback = feedback)
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(feedback: FeedbackItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                if (feedback.isAnonymous) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = "Anonymous",
                            tint = Color(0xFF64748B)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = feedback.initials,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = feedback.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = feedback.timeAgo,
                            fontSize = 12.sp,
                            color = if (feedback.isNew) Color(0xFF2D0981) else Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stars
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = if (index < feedback.rating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = feedback.major,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = feedback.comment,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )

                    // Reply button
                    if (feedback.showReply) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.clickable { /* Handle reply */ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Reply",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reply",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class FeedbackItem(
    val id: String,
    val isAnonymous: Boolean,
    val name: String,
    val initials: String,
    val profileImage: String?,
    val rating: Int,
    val major: String,
    val timeAgo: String,
    val isNew: Boolean,
    val comment: String,
    val year: String,
    val showReply: Boolean = false,
    val isLongText: Boolean = false
)