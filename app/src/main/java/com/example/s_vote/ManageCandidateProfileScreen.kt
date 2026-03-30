package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.s_vote.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.AppUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCandidateProfileScreen(navController: NavController, candidateId: String) {

    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Fetch profile on load
    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MY PROFILE", 
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
                Text("Profile not found.", color = TextSecondary)
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
                        .padding(bottom = 100.dp), // Space for bottom button
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 1. Top Section: Centered Profile Photo ---
                    Spacer(Modifier.height(32.dp))
                    
                    val photoUrl = details?.photo?.let {
                        if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it"
                    }
                    
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.05f),
                            border = BorderStroke(2.dp, Primary)
                        ) {
                            coil.compose.AsyncImage(
                                model = photoUrl ?: R.drawable.candidates,
                                contentDescription = "Profile Photo",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- 2. Name & Basic Info ---
                    Text(
                        user.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        (details?.position ?: "Candidate").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    
                    if (!details?.tagline.isNullOrBlank()) {
                        Text(
                            "\"${details!!.tagline}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))

                    // --- 3. Symbol Section ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, OutlineColor, RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Text(
                            "ELECTION SYMBOL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        val symbolUrl = details?.symbolUrl?.let {
                            if (it.startsWith("http")) it else "${com.example.s_vote.api.ApiClient.BASE_URL}$it"
                        }
                        
                        if (symbolUrl != null) {
                            coil.compose.AsyncImage(
                                model = symbolUrl,
                                contentDescription = "Symbol",
                                modifier = Modifier.size(100.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Default Symbol",
                                tint = Primary,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text(
                            details?.symbolName ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- 4. Personal & Campaign Details ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoRow(label = "DEPARTMENT", value = details?.course ?: user.department ?: "Not specified")
                        InfoRow(label = "COLLEGE", value = details?.college ?: user.college ?: "Not specified")
                        
                        Spacer(Modifier.height(8.dp))
                        
                        DetailBlock(title = "MY MANIFESTO 📜", content = details?.manifesto ?: "No manifesto available.")
                        DetailBlock(title = "CAMPAIGN GOALS 🎯", content = details?.goals ?: "No goals specified.")
                        DetailBlock(title = "MY PLEDGE 🤝", content = details?.pledges ?: "No pledges specified.")
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }

                // --- 5. Bottom "EDIT PROFILE" Button ---
                Button(
                    onClick = { navController.navigate("edit_candidate_profile/${candidateId}") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text("EDIT PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
    }
}

@Composable
fun DetailBlock(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight)
            .border(1.dp, OutlineColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Primary,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}
