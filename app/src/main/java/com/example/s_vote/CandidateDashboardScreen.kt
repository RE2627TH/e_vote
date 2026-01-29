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
import com.example.s_vote.ui.theme.*
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
                title = { 
                    Text(
                        "DASHBOARD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(24.dp),
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
                                    text = (profile?.name ?: "Unknown").uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = (candidateDesc?.position ?: "Candidate").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Status Badge
                                val status = candidateDesc?.status ?: "Pending"
                                val isApproved = status.equals("approved", true)
                                val badgeColor = if (isApproved) Success else Warning
                                val badgeBg = if (isApproved) Success.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f)

                                Surface(
                                    color = badgeBg,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
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
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Campaign",
                                    tint = Primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "VOTING IS LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "Polls are open from 9 AM to 5 PM. Your symbol is now visible to all voters on the ballot.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
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
                            "CAMPAIGN OVERVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
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
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
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
                                        "COLLEGE ELECTION 2025",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            "${candidateDesc?.voteCount ?: 0}", 
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "VOTES RECEIVED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                                
                                // Symbol Box
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BackgroundLight)
                                        .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
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
                                Text("VOTE SHARE GOAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = Primary,
                                trackColor = BackgroundLight,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---------------- QUICK ACTIONS ----------------
                    Text(
                        "QUICK ACTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp,
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
                                color = Success, // Emerald
                                bg = Success.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Routes.RESULT) } // Or Routes.RESULT
                            )
                            // Button 4: Edit Details
                            QuickActionButton(
                                icon = Icons.Default.Edit,
                                label = "Edit",
                                color = Secondary, // Indigo 400
                                bg = Secondary.copy(alpha = 0.1f),
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
                            "RECENT FEEDBACK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = { navController.navigate("candidate_feedback/${candidateId}") }) {
                            Text("VIEW ALL", color = Secondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
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
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor),
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
                text = label.uppercase(),
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun FeedbackItemCard(item: com.example.s_vote.model.Feedback) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
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
                        item.userName.uppercase(),
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
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
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }
        }
    }
}
