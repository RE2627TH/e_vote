package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteConfirmationScreen(
    navController: NavController,
    candidateId: String,
    position: String,
    studentName: String,
    department: String,
    studentId: String
) {
    val context = LocalContext.current
    val voteViewModel: com.example.s_vote.viewmodel.VoteViewModel = viewModel()
    val voteState by voteViewModel.voteState.collectAsState()
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

    val candidateViewModel: com.example.s_vote.viewmodel.CandidateViewModel = viewModel()
    val candidates by candidateViewModel.candidates.collectAsState()
    
    LaunchedEffect(Unit) {
        if (candidates.isEmpty()) {
            candidateViewModel.fetchCandidates()
        }
    }
    
    val candidate = candidates.find { it.id == candidateId }
    val candidateName = candidate?.name ?: "Loading..."

    var currentUserStudentId by remember { mutableStateOf<String?>(null) }
    var isProfileLoading by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                val response = com.example.s_vote.api.RetrofitInstance.api.getProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    currentUserStudentId = response.body()!!.user.studentId
                } else {
                    fetchError = "Profile fetch failed"
                }
            } catch (e: Exception) {
                fetchError = "Verification error"
            } finally {
                isProfileLoading = false
            }
        } else {
             isProfileLoading = false
             fetchError = "Not logged in"
        }
    }

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
                    studentId.trim().equals(currentUserStudentId?.trim(), ignoreCase = true)

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0533),
            Color(0xFF2104A1),
            Color(0xFF6743FF)
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glows
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = 100.dp, x = 100.dp)
                    .background(Color(0xFF2E7D32).copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.BottomEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "REVIEW VOTE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Final Step: Confirm Your Choice",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Candidate Glass Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "YOUR SELECTION", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            candidateName.uppercase(), 
                            fontSize = 22.sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White
                        )
                        Text(
                            position.uppercase(), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF6743FF),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Voter Verified Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "VOTER IDENTITY VERIFIED", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black, 
                                color = Color(0xFF4CAF50),
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        VoterInfoRow("NAME", studentName)
                        VoterInfoRow("DEPT", department)
                        VoterInfoRow("ID", studentId)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (isProfileLoading) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Securing Session...", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                } else if (fetchError != null) {
                    Text("SECURITY ERROR", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(fetchError!!, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                } else if (isMatched) {
                    Text(
                        "You are authorized for this action.",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            voteViewModel.castVote(userId, candidateId, position)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(30.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (voteState is com.example.s_vote.viewmodel.VoteState.Loading) {
                             CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                             Text("CAST MY VOTE", fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Red.copy(alpha = 0.1f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SECURITY MISMATCH", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("Session identity does not match verified ID.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoterInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text("$label: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
        Text(value.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}
