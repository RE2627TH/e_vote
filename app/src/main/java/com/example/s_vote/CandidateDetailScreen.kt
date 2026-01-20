package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
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

    val candidate = candidates.find { it.id == candidateId }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scannedIdResult by (savedStateHandle?.getStateFlow<String?>("scanned_id", null)?.collectAsState() ?: remember { mutableStateOf(null) })
    val scannedNameResult by (savedStateHandle?.getStateFlow<String?>("scanned_name", null)?.collectAsState() ?: remember { mutableStateOf(null) })
    val scannedDeptResult by (savedStateHandle?.getStateFlow<String?>("scanned_dept", null)?.collectAsState() ?: remember { mutableStateOf(null) })
    val manualModeResult by (savedStateHandle?.getStateFlow<Boolean>("manual_mode", false)?.collectAsState() ?: remember { mutableStateOf(false) })

    var verifiedId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var isEditing by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var scannedName by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var scannedDept by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(scannedIdResult, scannedNameResult, scannedDeptResult, manualModeResult) {
        if (scannedIdResult != null || manualModeResult) {
            val id = scannedIdResult ?: ""
            verifiedId = id
            scannedName = scannedNameResult ?: ""
            scannedDept = scannedDeptResult ?: ""
            if (id.isEmpty() || manualModeResult) isEditing = true
            android.widget.Toast.makeText(context, "ID Scanned. Please verify and confirm.", android.widget.Toast.LENGTH_SHORT).show()
            scrollState.animateScrollTo(scrollState.maxValue)
            savedStateHandle?.remove<String>("scanned_id")
            savedStateHandle?.remove<String>("scanned_name")
            savedStateHandle?.remove<String>("scanned_dept")
            savedStateHandle?.remove<Boolean>("manual_mode")
        }
    }

    LaunchedEffect(Unit) {
        if (candidates.isEmpty()) {
            viewModel.fetchCandidates()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0533),
            Color(0xFF2104A1),
            Color(0xFF6743FF)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "CANDIDATE PROFILE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = (-50).dp, x = (-100).dp)
                    .background(Color(0xFFFF3DA6).copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.TopStart)
            )

            if (candidate == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(32.dp))

                    // Premium Profile Section (Glassmorphic)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(2.dp, Color(0xFF6743FF), CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                            ) {
                                val photoUrl = candidate.photo?.let { 
                                    if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it" 
                                }
                                
                                if (photoUrl != null) {
                                     coil.compose.AsyncImage(
                                        model = photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = candidate.imageResId ?: R.drawable.candidates),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                (candidate.name ?: "UNKNOWN").uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    (candidate.position ?: "CANDIDATE").uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Manifesto Section (Glassmorphic)
                    Text(
                        "CANDIDATE MANIFESTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            candidate.manifesto ?: "No manifesto provided.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Feedback Section
                    var showFeedbackDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "STUDENT REVIEWS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        TextButton(onClick = { showFeedbackDialog = true }) {
                            Text("ADD REVIEW", color = Color(0xFF6743FF), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        if (candidate.feedback.isNullOrEmpty()) {
                            Text("No reviews yet.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                        } else {
                            Column {
                                candidate.feedback.take(3).forEachIndexed { index, feedback ->
                                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = if(index < 2) 16.dp else 0.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(feedback.userName.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("★ ${feedback.rating}", fontSize = 11.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                                        }
                                        Text(feedback.comment, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                                        if (index < 2 && candidate.feedback.size > 1) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showFeedbackDialog) {
                        var rating by remember { mutableStateOf(0) }
                        var comment by remember { mutableStateOf("") }
                        
                        val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
                        val currentUserName = "Student" // Placeholder

                        AlertDialog(
                            onDismissRequest = { showFeedbackDialog = false },
                            containerColor = Color(0xFF1E0B6E),
                            title = { Text("RATE CANDIDATE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) },
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
                                        placeholder = { Text("Write your feedback...", color = Color.White.copy(alpha = 0.3f)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.White.copy(alpha = 0.4f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                        )
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("SUBMIT", fontWeight = FontWeight.Black)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showFeedbackDialog = false }) {
                                    Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Vote History Check & Action
                    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
                    val userId = sharedPref.getString("USER_ID", "") ?: ""
                    val historyViewModel: com.example.s_vote.viewmodel.VoteHistoryViewModel = viewModel()
                    val historyState by historyViewModel.historyState.collectAsState()
                    
                    LaunchedEffect(userId) {
                        if (userId.isNotEmpty()) {
                            historyViewModel.fetchVoteHistory(userId)
                        }
                    }

                    val hasVotedForThisPosition = if (historyState is com.example.s_vote.viewmodel.VoteHistoryState.Success) {
                        (historyState as com.example.s_vote.viewmodel.VoteHistoryState.Success).theHistory.any { 
                            it.position.equals(candidate.position, ignoreCase = true) 
                        }
                    } else false

                    if (hasVotedForThisPosition) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "VOTED SUCCESSFULLY",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val safeId = candidate?.id ?: ""
                                val safePosition = candidate?.position ?: ""
                                if (safeId.isNotEmpty()) {
                                    val encodedPos = java.net.URLEncoder.encode(safePosition, "UTF-8")
                                    navController.navigate("scan_id/$safeId?position=$encodedPos")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6743FF)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                            enabled = historyState !is com.example.s_vote.viewmodel.VoteHistoryState.Loading
                        ) {
                            if (historyState is com.example.s_vote.viewmodel.VoteHistoryState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    "CAST YOUR VOTE", 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
