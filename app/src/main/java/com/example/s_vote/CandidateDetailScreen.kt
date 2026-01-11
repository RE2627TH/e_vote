package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.CandidateViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailScreen(navController: NavController, candidateId: String) {

    val viewModel: CandidateViewModel = viewModel()
    val candidates by viewModel.candidates.collectAsState()

    // Find specific candidate
    val candidate = candidates.find { it.id == candidateId }

    // If candidate unknown (e.g. deep link or reload), fetch again
    LaunchedEffect(Unit) {
        if (candidates.isEmpty()) {
            viewModel.fetchCandidates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        if (candidate == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Profile Image with Gradient Border
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6A4CFF), Color(0xFFE04EF6))
                            )
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                ) {
                    val photoUrl = candidate.photo?.let { 
                        if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it" 
                    }
                    
                    if (photoUrl != null) {
                         coil.compose.AsyncImage(
                            model = photoUrl,
                            contentDescription = candidate.name ?: "Candidate Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                            contentDescription = candidate.name ?: "Candidate Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = candidate.name ?: "Unknown Name",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = candidate.position ?: "No Position",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                
                 Text(
                    text = candidate.department ?: "N/A",
                    fontSize = 14.sp,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Cards
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Manifesto", fontWeight = FontWeight.Bold, color = Color(0xFF3E1F7F))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(candidate.manifesto ?: "No manifesto available.", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Feedback Section
                var showFeedbackDialog by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Student Feedback", fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                            TextButton(onClick = { showFeedbackDialog = true }) {
                                Text("Write a Review", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (candidate.feedback.isNullOrEmpty()) {
                            Text("No feedback yet.", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            candidate.feedback.take(3).forEach { feedback ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(feedback.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("⭐ ${feedback.rating}", fontSize = 12.sp, color = Color(0xFFFFB300))
                                    }
                                    Text(feedback.comment, fontSize = 13.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                if (showFeedbackDialog) {
                    var rating by remember { mutableStateOf(0) }
                    var comment by remember { mutableStateOf("") }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    // Fetch current user name (Simplified for now, can be improved to fetch from Profile API)
                    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
                    val currentUserName = "Student" // Placeholder, ideally fetch from profile

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
                                                imageVector = if (i <= rating) androidx.compose.material.icons.Icons.Default.Star else androidx.compose.material.icons.Icons.Default.StarBorder,
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
                                        viewModel.submitFeedback(candidate.id ?: "", currentUserName, rating, comment) { success, msg ->
                                             android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                             if (success) showFeedbackDialog = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2104A1))
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

                Spacer(modifier = Modifier.height(32.dp))

                // Vote Button
                Button(
                    onClick = {
                        val safeId = candidate.id ?: ""
                        val safePosition = candidate.position ?: ""
                        if (safeId.isNotEmpty()) {
                            navController.navigate(
                                Routes.SCAN_ID
                                .replace("{candidateId}", safeId)
                                + "?position=$safePosition"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2104A1)
                    )
                ) {
                    Text("Vote for ${candidate.name ?: "Candidate"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
