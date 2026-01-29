package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import coil.compose.AsyncImage
import com.example.s_vote.api.ApiClient
import androidx.compose.ui.layout.ContentScale
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListingScreen(navController: NavController, roleName: String) {

    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = viewModel()
    val candidates by viewModel.candidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCandidates()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BackgroundLight,
            SurfaceLight
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        roleName.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
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
        bottomBar = {
            BottomNavBar(navController = navController, selectedRoute = Routes.POLL_HISTORY)
        },
        containerColor = BackgroundLight
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glow - Subtler
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = 100.dp, x = 200.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape)
                    .align(Alignment.BottomEnd)
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    val filteredCandidates = candidates.filter {
                        val isApproved = it.status.equals("approved", ignoreCase = true)
                        val matchesRole = (it.position ?: "").lowercase().replace(" ", "_") == roleName.lowercase() ||
                                (it.role ?: "").lowercase() == roleName.lowercase()
                        isApproved && matchesRole
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                    ) {
                        item {
                            // Premium Light Info Banner
                             Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SurfaceLight)
                                    .border(
                                        1.dp,
                                        OutlineColor,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .padding(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_thumb_up),
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                         Text(
                                            "CANDIDATE DIRECTORY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            "Review profiles and manifestos carefully",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "AVAILABLE PROFILES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${filteredCandidates.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Secondary
                                    )
                                }
                            }
                        }

                        items(filteredCandidates) { candidate ->
                            CandidateCardPremium(
                                candidate = candidate,
                                onClick = {
                                    val safeId = candidate.id ?: ""
                                    if (safeId.isNotEmpty()) {
                                        navController.navigate(Routes.CANDIDATE_DETAIL.replace("{id}", safeId))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateCardPremium(candidate: com.example.s_vote.model.Candidate, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLight)
            .border(
                1.dp,
                OutlineColor,
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Image with ring and Symbol Overlay
            Box(
                modifier = Modifier
                    .size(64.dp)
            ) {
                // Main Profile Image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                        .border(1.dp, OutlineColor, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                ) {
                    val photoUrl = candidate.photo?.let {
                        if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                    }
                    AsyncImage(
                        model = photoUrl ?: candidate.imageResId ?: R.drawable.candidates,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Symbol Overlay "Ring"
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(4.dp)
                ) {
                    val symbolUrl = candidate.symbolUrl?.let {
                        if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                    }
                    AsyncImage(
                        model = symbolUrl ?: candidate.symbolResId ?: Icons.Default.CheckCircle,
                        contentDescription = "Symbol",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (candidate.symbolUrl == null && candidate.symbolResId == null)
                            ColorFilter.tint(Color.White) else null
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (candidate.name ?: "Unknown").uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    (candidate.position ?: "Candidate").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                if (!candidate.badges.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        candidate.badges?.take(2)?.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Primary.copy(alpha = 0.05f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                 Text(
                                    badge.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Secondary
                                )
                            }
                        }
                    }
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = OutlineColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
