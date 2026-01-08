package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes


@Composable
fun IdVerifiedScreen(
    navController: NavController,
    candidateId: String,
    studentName: String,
    department: String,
    studentId: String,
    position: String
) {
    // ViewModel Setup
    val context = androidx.compose.ui.platform.LocalContext.current
    val voteViewModel: com.example.s_vote.viewmodel.VoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val voteState by voteViewModel.voteState.collectAsState()
    
    // User ID from SharedPrefs
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

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

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedRoute = Routes.RESULT
            )
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
            // ... (Image and Texts remain same, just updating Button logic)

            Image(
                painter = painterResource(id = R.drawable.ic_thumb_up),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF3E1F7F))
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2EFFA))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Successfully verified with ID",
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_thumb_up),
                    contentDescription = "Verified",
                    tint = Color(0xFF3E1F7F),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = studentName,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Student Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = department,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Department") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = studentId,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Student ID") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "Status: Verified",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("ID Status") },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scan),
                        contentDescription = "Scanned"
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "Selected Candidate ID: $candidateId\nPosition: $position",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Vote Details") },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_thumb_up),
                        contentDescription = "Candidate"
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // Verification Logic
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
                            fetchError = "Failed to fetch profile for verification"
                        }
                    } catch (e: Exception) {
                        fetchError = "Error verifying ID: ${e.message}"
                    } finally {
                        isProfileLoading = false
                    }
                }
            }

            val isMatched = !currentUserStudentId.isNullOrEmpty() && 
                           studentId.equals(currentUserStudentId, ignoreCase = true)

            if (isProfileLoading) {
                CircularProgressIndicator()
                Text("Verifying ID against profile...")
            } else if (fetchError != null) {
                Text(text = fetchError!!, color = Color.Red)
            } else if (isMatched) {
                Text("✅ ID Matches Logged-in User", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                
                 Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        voteViewModel.castVote(userId, candidateId, position)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    colors = ButtonDefaults.buttonColors(Color(0xFF6743FF))
                ) {
                    if (voteState is com.example.s_vote.viewmodel.VoteState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Confirm & Submit Vote", color = Color.White)
                    }
                }
            } else {
                Text("❌ ID Mismatch!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Scanned: $studentId", color = Color.Gray)
                Text("Profile: ${currentUserStudentId ?: "Not found"}", color = Color.Gray)
                Text("You can only use your own ID card.", color = Color.Red)
            }
        }
    }
}
