package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes

@Composable
fun VoteConfirmationScreen(
    navController: NavController,
    candidateId: String,
    position: String,
    studentName: String,
    department: String,
    studentId: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voteViewModel: com.example.s_vote.viewmodel.VoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val voteState by voteViewModel.voteState.collectAsState()
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

    // Candidate Lookup
    // Candidate Lookup using ViewModel
    val candidateViewModel: com.example.s_vote.viewmodel.CandidateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val candidates by candidateViewModel.candidates.collectAsState()
    
    LaunchedEffect(Unit) {
        if (candidates.isEmpty()) {
            candidateViewModel.fetchCandidates()
        }
    }
    
    val candidate = candidates.find { it.id == candidateId }
    val candidateName = candidate?.name ?: "Loading..."

    // Verification Logic Steps
    var currentUserStudentId by remember { mutableStateOf<String?>(null) }
    var isProfileLoading by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    // Fetch user profile to verify ID matches logged in user
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                val response = com.example.s_vote.api.RetrofitInstance.api.getProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    currentUserStudentId = response.body()!!.user.studentId
                } else {
                    fetchError = "Failed to fetch profile for verification"
                }
            } catch (e: Exception) {
                fetchError = "Error verifying ID: ${e.message}"
            } finally {
                isProfileLoading = false
            }
        } else {
             isProfileLoading = false
             fetchError = "User not logged in"
        }
    }

    // Handle Vote State
    LaunchedEffect(voteState) {
        when(voteState) {
            is com.example.s_vote.viewmodel.VoteState.Success -> {
                 navController.navigate(Routes.VOTE_SUBMITTED) {
                     popUpTo(Routes.HOME) { inclusive = false }
                 }
                 voteViewModel.resetState()
            }
            is com.example.s_vote.viewmodel.VoteState.Error -> {
                android.widget.Toast.makeText(context, (voteState as com.example.s_vote.viewmodel.VoteState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                voteViewModel.resetState()
            }
            else -> {}
        }
    }

    val isMatched = !currentUserStudentId.isNullOrEmpty() && 
                    studentId.equals(currentUserStudentId, ignoreCase = true)

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, selectedRoute = Routes.HOME)
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
                "Vote Confirmation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E1F7F),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Candidate Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFFA)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Voting For", fontSize = 14.sp, color = Color.Gray)
                    Text(candidateName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E1F7F))
                    Text(position, fontSize = 16.sp, color = Color(0xFF6743FF))
                }
            }

            // Scanned ID Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.ic_scan), contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Identity Verified", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: $studentName")
                    Text("Dept: $department")
                    Text("ID: $studentId")
                }
            }

            // Verification Status & Action
            if (isProfileLoading) {
                CircularProgressIndicator()
                Text("Verifying User Identity...", modifier = Modifier.padding(top = 8.dp))
            } else if (fetchError != null) {
                Text("Error: $fetchError", color = Color.Red)
            } else if (isMatched) {
                Text(
                    "You are eligible to vote.",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        voteViewModel.castVote(userId, candidateId, position)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF))
                ) {
                    if (voteState is com.example.s_vote.viewmodel.VoteState.Loading) {
                         CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                         Text("CONFIRM VOTE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("❌ ID Mismatch!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Scanned ID ($studentId) does not match logged-in user.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
