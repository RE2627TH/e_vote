package com.example.s_vote

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.s_vote.viewmodel.AdminViewModel
import com.example.s_vote.ui.theme.*
import com.example.s_vote.api.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCandidateReviewScreen(navController: NavController, candidateId: String) {

    val candidateViewModel: CandidateViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    
    val profile by candidateViewModel.profile.collectAsState()
    val isLoading by candidateViewModel.isLoading.collectAsState()
    val isAdminLoading by adminViewModel.isLoading.collectAsState()
    val message by adminViewModel.message.collectAsState()

    LaunchedEffect(candidateId) {
        candidateViewModel.fetchProfile(candidateId)
    }

    // Handle Success Navigation
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(message) {
        if (message?.contains("successfully", ignoreCase = true) == true) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessage()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "REVIEW CANDIDATE", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
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
                CircularProgressIndicator(color = Primary)
            }
        } else if (profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Candidate details not found", color = Color.Gray)
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
                        .padding(bottom = 100.dp) // Space for bottom buttons
                ) {
                    // --- HEADER SECTION ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Primary.copy(alpha = 0.1f), BackgroundLight)
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             val photoUrl = details?.photo?.let {
                                 if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                             }
                             AsyncImage(
                                model = photoUrl ?: R.drawable.candidates,
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Primary, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                user.name.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                (details?.position ?: "Candidate").uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // --- CONTENT SECTION ---
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        
                        ReviewInfoRow(label = "DEPARTMENT", value = details?.course ?: user.department ?: "N/A")
                        ReviewInfoRow(label = "COLLEGE", value = details?.college ?: user.college ?: "N/A")
                        ReviewInfoRow(label = "STUDENT ID", value = user.studentId ?: "N/A")
                        
                        Spacer(Modifier.height(24.dp))

                        ReviewDetailBlock(title = "MANIFESTO 📜", content = details?.manifesto ?: "No manifesto.")
                        ReviewDetailBlock(title = "CAMPAIGN GOALS 🎯", content = details?.goals ?: "No goals specified.")
                        ReviewDetailBlock(title = "MY PLEDGE 🤝", content = details?.pledges ?: "No pledges specified.")

                        Spacer(Modifier.height(24.dp))

                        // Symbol Preview
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val symbolUrl = details?.symbolUrl?.let {
                                    if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BackgroundLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = symbolUrl ?: R.drawable.ic_launcher_foreground,
                                        contentDescription = "Symbol",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("ELECTION SYMBOL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text(details?.symbolName ?: "N/A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // --- STICKY BOTTOM ACTIONS ---
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = BackgroundLight,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Reject Button
                        Button(
                            onClick = { adminViewModel.rejectCandidate(user.id) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isAdminLoading
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFB91C1C))
                            Spacer(Modifier.width(8.dp))
                            Text("REJECT", color = Color(0xFFB91C1C), fontWeight = FontWeight.Black)
                        }

                        // Accept Button
                        Button(
                            onClick = { adminViewModel.approveCandidate(user.id) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isAdminLoading
                        ) {
                            if (isAdminLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("APPROVE", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
    }
}

@Composable
fun ReviewDetailBlock(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            title, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Black, 
            color = Primary,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceLight,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Primary.copy(alpha = 0.05f))
        ) {
            Text(
                content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = TextPrimary
            )
        }
    }
}
