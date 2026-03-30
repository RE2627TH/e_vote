package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.AdminViewModel
import com.example.s_vote.api.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCandidateListScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel()
    val candidateUsers by viewModel.candidateUserList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchCandidateUserList()
    }

    val context = LocalContext.current
    LaunchedEffect(message) {
        message?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val filteredList = candidateUsers.filter { user ->
        query.isBlank() || 
        user.name.contains(query, ignoreCase = true) || 
        (user.department ?: "").contains(query, ignoreCase = true) ||
        (user.position ?: "").contains(query, ignoreCase = true) ||
        (user.studentId ?: "").contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CANDIDATE LIST",
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
                actions = {
                    IconButton(onClick = { viewModel.fetchCandidateUserList() }) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Primary)
                        } else {
                            Icon(painter = painterResource(android.R.drawable.stat_notify_sync), contentDescription = "Refresh", tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name, position or dept...", color = TextSecondary.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(painter = painterResource(android.R.drawable.ic_menu_search), contentDescription = "Search", tint = Primary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading && candidateUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No candidates found", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList) { candidateItem ->
                        CandidateCard(candidateItem) {
                            navController.navigate(Routes.adminStudentDetail(candidateItem.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateCard(candidate: com.example.s_vote.model.AppUser, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo or Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(BackgroundLight)
                    .border(1.dp, OutlineColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!candidate.profilePhoto.isNullOrBlank()) {
                    val fullUrl = if (candidate.profilePhoto.startsWith("http")) candidate.profilePhoto else "${ApiClient.BASE_URL}${candidate.profilePhoto}"
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = candidate.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.candidates)
                    )
                } else {
                    Text(
                        text = candidate.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = (candidate.position ?: "Candidate").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = (candidate.department ?: "Unknown Dept").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (candidate.applicationStatus == "ACCEPTED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = candidate.applicationStatus ?: "PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (candidate.applicationStatus == "ACCEPTED") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
