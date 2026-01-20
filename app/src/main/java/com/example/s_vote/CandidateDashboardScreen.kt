package com.example.s_vote

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.api.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDashboardScreen(navController: NavController, candidateId: String) {

    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F8))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content if error/loading
        ) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                // Show Error
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Unknown Error",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchProfile(candidateId) }) {
                        Text("Retry")
                    }
                }
            } else if (profile == null) {
                 // Should not happen if loading is false and no error, but fallback
                Text("No Profile Data Found")
            } else {


                // =====================================================================
                //                              MAIN CONTENT
                // =====================================================================

                val candidateDesc = profile?.candidateDetails
                val scrollState = androidx.compose.foundation.rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // ---------------- HEADER SECTION ----------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Profile Image with Symbol Overlay
                            Box(modifier = Modifier.padding(end = 16.dp)) {
                                // Profile Image
                                val photoUrl = candidateDesc?.photo?.let { path ->
                                    if (path.startsWith("http")) path else "${ApiClient.BASE_URL}$path"
                                }
                                
                                if (photoUrl != null) {
                                    coil.compose.AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Profile",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.White, CircleShape)
                                            .shadow(4.dp, CircleShape)
                                    )
                                } else {
                                    Image(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0E0E0))
                                            .border(2.dp, Color.White, CircleShape)
                                            .padding(12.dp)
                                    )
                                }

                            // Symbol Overlay (Bottom Right)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C097F))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Symbol",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Name & Details
                            Column {
                                Text(
                                    text = profile?.name ?: "Unknown",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111318)
                                )
                                Text(
                                    text = candidateDesc?.position ?: "Candidate",
                                    fontSize = 14.sp,
                                    color = Color(0xFF616F89),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Status Badge
                                val status = candidateDesc?.status ?: "Pending"
                                val isApproved = status.equals("approved", true)
                                val badgeColor = if (isApproved) Color(0xFF198754) else Color(0xFFFFA000)
                                val badgeBg = if (isApproved) Color(0xFFD1E7DD) else Color(0xFFFFF3E0)

                                Surface(
                                    color = badgeBg,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if(isApproved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = badgeColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = status.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ---------------- NOTIFICATION BANNER ----------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2C097F).copy(alpha = 0.1f),
                                        Color(0xFF2C097F).copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF2C097F).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C097F).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign, // Make sure to import Icons.Default.Campaign or similar
                                    contentDescription = "Campaign",
                                    tint = Color(0xFF2C097F)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Voting is live today!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111318)
                                )
                                Text(
                                    "Polls are open from 9 AM to 5 PM. Your symbol is now visible to all voters on the ballot.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF616F89),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------------- CAMPAIGN OVERVIEW ----------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Campaign Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111318)
                        )
                        Surface(
                            color = Color(0xFF2C097F).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2C097F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Shadow-sm equivalent
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        "College Election 2025",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF616F89)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            "${candidateDesc?.voteCount ?: 0}", 
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111318)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "votes received",
                                            fontSize = 13.sp,
                                            color = Color(0xFF616F89),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }
                                
                                // Symbol Box
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF6F6F8))
                                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    val symbolUrl = candidateDesc?.symbolUrl?.let {
                                        if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                                    }
                                    coil.compose.AsyncImage(
                                        model = symbolUrl ?: Icons.Default.CheckCircle, 
                                        contentDescription = "Symbol",
                                        modifier = Modifier.size(32.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                        colorFilter = if (candidateDesc?.symbolUrl == null) androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF2C097F)) else null
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Your Symbol",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF616F89),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Progress Bar Section
                            val voteGoal = 500
                            val currentVotes = candidateDesc?.voteCount ?: 0
                            val progress = (currentVotes.toFloat() / voteGoal.toFloat()).coerceIn(0f, 1f)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vote Share Goal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616F89))
                                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C097F))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Color(0xFF2C097F),
                                trackColor = Color(0xFFF0F2F4),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------------- QUICK ACTIONS ----------------
                    Text(
                        "Quick Actions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111318),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Grid of 4 buttons
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Button 1: View Profile
                            QuickActionButton(
                                icon = Icons.Default.Visibility,
                                label = "View Profile",
                                color = Color(0xFF2563EB), // Blue
                                bg = Color(0xFFEFF6FF),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("candidate_profile/${candidateId}") }
                            )
                            // Button 2: Feedback
                            QuickActionButton(
                                icon = Icons.Default.Reviews,
                                label = "Feedback",
                                color = Color(0xFF9333EA), // Purple
                                bg = Color(0xFFFAF5FF),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("candidate_feedback/${candidateId}") }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Button 3: Results
                            QuickActionButton(
                                icon = Icons.Default.Analytics,
                                label = "Results",
                                color = Color(0xFFEA580C), // Orange
                                bg = Color(0xFFFFF7ED),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Routes.RESULT) } // Or Routes.RESULT
                            )
                            // Button 4: Edit Details
                            QuickActionButton(
                                icon = Icons.Default.Edit,
                                label = "Edit Details",
                                color = Color(0xFF16A34A), // Green
                                bg = Color(0xFFF0FDF4),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("candidate_profile/${candidateId}") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------------- RECENT FEEDBACK ----------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Feedback",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111318)
                        )
                        TextButton(onClick = { navController.navigate("candidate_feedback/${candidateId}") }) {
                            Text("View All", color = Color(0xFF2C097F), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Feedback List (Mock or Empty State if no data)
                    if (candidateDesc?.feedback.isNullOrEmpty()) {
                         Text("No feedback yet.", fontSize = 14.sp, color = Color.Gray)
                    } else {
                         candidateDesc!!.feedback!!.take(2).forEach { feedbackItem ->
                             FeedbackItemCard(feedbackItem)
                             Spacer(modifier = Modifier.height(12.dp))
                         }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// ---------------- HELPER COMPOSABLES ----------------

@Composable
fun QuickActionButton(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF111318)
            )
        }
    }
}

@Composable
fun FeedbackItemCard(item: com.example.s_vote.model.Feedback) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
             Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                     item.userName.take(1),
                     fontWeight = FontWeight.Bold,
                     color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        item.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF111318)
                    )
                    Row {
                         repeat(item.rating.toInt()) {
                              Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                         }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.comment,
                    fontSize = 13.sp,
                    color = Color(0xFF616F89),
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }
        }
    }
}
