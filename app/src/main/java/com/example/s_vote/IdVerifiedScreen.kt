package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*


@Composable
fun IdVerifiedScreen(
    navController: NavController,
    candidateId: String,
    studentName: String,
    department: String,
    studentId: String,
    position: String
) {
    var editableName by remember { mutableStateOf(studentName) }
    var editableDept by remember { mutableStateOf(department) }
    var editableId by remember { mutableStateOf(studentId) }
    var isConfirmed by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val voteViewModel: com.example.s_vote.viewmodel.VoteViewModel = viewModel()
    val voteState by voteViewModel.voteState.collectAsState()
    
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

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

    val scrollState = rememberScrollState()
    
    LaunchedEffect(isConfirmed) {
        if (isConfirmed) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Background is now BackgroundLight via Scaffold

    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Glows
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = (-100).dp, x = (-100).dp)
                    .background(Primary.copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                
                // Premium Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(SurfaceLight)
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_thumb_up),
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp),
                        colorFilter = ColorFilter.tint(Primary)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "ID VERIFICATION",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        "REVIEW AND CONFIRM SCANNED DATA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Success Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, SuccessMild.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SuccessMild.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessMild, modifier = Modifier.size(24.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                "SCAN SUCCESSFUL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessMild
                            )
                            Text(
                                "Auto-extracted ID details",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ModernVerifiedTextField(
                    value = editableName, 
                    onValueChange = { editableName = it }, 
                    label = "STUDENT NAME"
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModernVerifiedTextField(
                    value = editableDept, 
                    onValueChange = { editableDept = it }, 
                    label = "DEPARTMENT / COLLEGE"
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModernVerifiedTextField(
                    value = editableId, 
                    onValueChange = { 
                        editableId = it
                        isConfirmed = false
                    }, 
                    label = "STUDENT ID / ROLL NO.",
                    supportingText = "Ensure this matches your physical ID card."
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isConfirmed) {
                    Button(
                        onClick = { isConfirmed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("VERIFY MY IDENTITY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                if (isConfirmed) {
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
                                    fetchError = "Profile fetch failed"
                                }
                            } catch (e: Exception) {
                                fetchError = "Verification error"
                            } finally {
                                isProfileLoading = false
                            }
                        }
                    }

                    val isMatched = !currentUserStudentId.isNullOrEmpty() && 
                                   editableId.trim().equals(currentUserStudentId?.trim(), ignoreCase = true)

                    if (isProfileLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else if (fetchError != null) {
                        Text(text = "CONNECTION ERROR", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        TextButton(onClick = { isConfirmed = false }) { Text("RETRY", color = Color.White) }
                    } else if (isMatched) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SuccessMild.copy(alpha = 0.05f))
                                .border(1.dp, SuccessMild.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessMild, modifier = Modifier.size(40.dp))
                                Text("AUTHORIZATION GRANTED", style = MaterialTheme.typography.titleSmall, color = SuccessMild, fontWeight = FontWeight.Bold)
                                Text("Your ID matches your registered profile.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                val encName = java.net.URLEncoder.encode(editableName, "UTF-8")
                                val encDept = java.net.URLEncoder.encode(editableDept, "UTF-8")
                                val encId = java.net.URLEncoder.encode(editableId, "UTF-8")
                                val encPos = java.net.URLEncoder.encode(position, "UTF-8")
                                navController.navigate("vote_confirmation/$candidateId?position=$encPos&name=$encName&dept=$encDept&id=$encId")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessMild)
                        ) {
                            Text("PROCEED TO VOTE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Red.copy(alpha = 0.1f))
                                .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ID MISMATCH", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Registered ID: ${currentUserStudentId ?: "N/A"}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("Entered ID: $editableId", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("Please ensure accuracy before proceeding.", color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp)
                                TextButton(onClick = { isConfirmed = false }) {
                                    Text("RE-EDIT DATA", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ModernVerifiedTextField(value: String, onValueChange: (String) -> Unit, label: String, supportingText: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Primary.copy(alpha = 0.2f),
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary
            )
        )
        if (supportingText != null) {
            Text(
                supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
