package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s_vote.viewmodel.CandidateViewModel
import coil.compose.AsyncImage
import com.example.s_vote.api.ApiClient
import androidx.compose.material.icons.filled.FormatQuote
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestoScreen(navController: NavController) {
    val viewModel: CandidateViewModel = viewModel()
    val candidates by viewModel.candidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCandidates()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CANDIDATE MANIFESTOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else {
                val manifestoCandidates = candidates.filter { 
                    it.status.equals("approved", ignoreCase = true) && !it.manifesto.isNullOrBlank() 
                }

                if (manifestoCandidates.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No manifestos available yet.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            Text(
                                "VISIONARY LEADERSHIP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextSecondary,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        items(manifestoCandidates) { candidate ->
                            ManifestoCard(candidate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManifestoCard(candidate: com.example.s_vote.model.Candidate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Candidate Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                val photoUrl = candidate.photo?.let {
                    if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                }
                
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = SurfaceVariant
                ) {
                    AsyncImage(
                        model = photoUrl ?: R.drawable.candidates,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        (candidate.name ?: "Unknown").uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        (candidate.position ?: "Candidate").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Highlighted Manifesto Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.05f))
                    .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Text(
                        text = candidate.manifesto ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(2.dp)
                            .width(40.dp)
                            .background(Primary)
                    )
                }
            }
        }
    }
}
