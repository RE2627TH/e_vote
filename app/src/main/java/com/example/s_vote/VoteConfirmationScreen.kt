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
import androidx.compose.ui.draw.shadow
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
import com.example.s_vote.ui.theme.*

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

    // Background is now BackgroundLight via Scaffold

    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Subtler Glow
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = 100.dp, x = 100.dp)
                    .background(Primary.copy(alpha = 0.05f), CircleShape)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Final Step: Confirm Your Choice",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Candidate Premium Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "YOUR SELECTION", 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, 
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            candidateName.uppercase(), 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold, 
                            color = TextPrimary
                        )
                        Text(
                            position.uppercase(), 
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, 
                            color = Primary,
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
                        .background(SuccessMild.copy(alpha = 0.05f))
                        .border(1.dp, SuccessMild.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessMild, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "VOTER IDENTITY VERIFIED", 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold, 
                                color = SuccessMild,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        VoterInfoRow("NAME", studentName)
                        Spacer(Modifier.height(8.dp))
                        VoterInfoRow("DEPT", department)
                        Spacer(Modifier.height(8.dp))
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
                        color = SuccessMild,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            voteViewModel.castVote(userId, candidateId, position)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (voteState is com.example.s_vote.viewmodel.VoteState.Loading) {
                             CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                             Text("CAST MY VOTE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f))
                            .border(1.dp, androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SECURITY MISMATCH", color = androidx.compose.ui.graphics.Color.Red, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Session identity does not match verified ID.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoterInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label: ", 
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, 
            color = TextSecondary,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            value.uppercase(), 
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold, 
            color = TextPrimary,
            modifier = Modifier.weight(0.7f)
        )
    }
}
