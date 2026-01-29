package com.example.s_vote

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.CandidateViewModel
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateProfileViewScreen(navController: NavController, candidateId: String) {

    val viewModel: CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Candidate Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Candidate not found", color = Color.Gray)
            }
        } else {
            val user = profile!!
            val details = user.candidateDetails

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(BackgroundLight)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 120.dp)
                ) {
                    // --- HEADER ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        // Soft Deep Gradient Background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Primary.copy(alpha = 0.15f), BackgroundLight)
                                    )
                                )
                        )
                        
                        // Profile Info
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             val photoUrl = details?.photo?.let {
                                 if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it"
                             }
                             AsyncImage(
                                model = photoUrl ?: R.drawable.candidates,
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundLight)
                                    .border(2.dp, Primary, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                user.name.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                (details?.position ?: "Candidate").uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // --- DETAILS ---
                    Column(Modifier.padding(16.dp)) {

                        // Manifesto
                        CardSection(title = "Manifesto 📜") {
                             Text(
                                details?.manifesto ?: "No manifesto provided.",
                                lineHeight = 20.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Goals
                        CardSection(title = "Campaign Goals 🎯") {
                             Text(
                                details?.goals ?: "No specific goals listed.",
                                lineHeight = 20.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        // Pledges
                         CardSection(title = "My Pledge 🤝") {
                             Text(
                                details?.pledges ?: "I pledge to serve with integrity.",
                                lineHeight = 20.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextPrimary
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))

                        // Symbol
                        CardSection(title = "Election Symbol") {
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                  val symbolUrl = details?.symbolUrl?.let {
                                      if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it"
                                  }
                                  AsyncImage(
                                    model = symbolUrl ?: R.drawable.ic_launcher_foreground,
                                    contentDescription = "Symbol",
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text("This is my symbol on the ballot.")
                             }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Feedback Section
                        var showFeedbackDialog by remember { mutableStateOf(false) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Feedback & Ratings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                                    TextButton(onClick = { showFeedbackDialog = true }) {
                                        Text("Rate Candidate", color = Secondary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val feedbackList = details?.feedback
                                if (feedbackList.isNullOrEmpty()) {
                                    Text("No reviews yet. Be the first to review!", fontSize = 14.sp, color = Color.Gray)
                                } else {
                                    val averageRating = feedbackList.map { it.rating }.average()
                                    Text("Average Rating: ${String.format("%.1f", averageRating)} ⭐", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                     feedbackList.take(5).forEach { feedback ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(feedback.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("${feedback.rating} ⭐", fontSize = 12.sp, color = Color(0xFFFFB300))
                                            }
                                            Text(feedback.comment, fontSize = 14.sp, color = TextSecondary)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Primary.copy(alpha = 0.1f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (showFeedbackDialog) {
                            var rating by remember { mutableIntStateOf(0) }
                            var comment by remember { mutableStateOf("") }
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
                            val currentUserName = sharedPref.getString("USER_NAME", "Student") ?: "Student"

                            AlertDialog(
                                onDismissRequest = { showFeedbackDialog = false },
                                title = { Text("Rate Candidate") },
                                text = {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            for (i in 1..5) {
                                                IconButton(onClick = { rating = i }) {
                                                    Icon(
                                                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = "Star $i",
                                                        tint = Color(0xFFFFB300)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedTextField(
                                            value = comment,
                                            onValueChange = { comment = it },
                                            label = { Text("Leave a comment") },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 3
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (rating == 0) {
                                                android.widget.Toast.makeText(context, "Please select a rating", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.submitFeedback(user.id, currentUserName, rating, comment) { success, msg ->
                                                     android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                     if (success) {
                                                          showFeedbackDialog = false
                                                          viewModel.fetchProfile(candidateId) // Refresh profile to show new review
                                                     }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                    ) {
                                        Text("Submit")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showFeedbackDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                } // End Scrollable Column

                // --- VOTE BUTTON ---
                Button(
                    onClick = {
                        val pos = details?.position ?: ""
                        val encodedPos = java.net.URLEncoder.encode(pos, "UTF-8")
                        navController.navigate("scan_id/$candidateId?position=$encodedPos")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessMild),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("VOTE FOR ${user.name.uppercase()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun CardSection(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
             Spacer(Modifier.height(12.dp))
             content()
        }
    }
}