package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminElectionListScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel()
    val elections by viewModel.elections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchElections()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "SELECT ELECTION", 
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
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading && elections.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (elections.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.HowToVote, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = TextMuted
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No elections created yet", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(elections) { election ->
                    ElectionSelectCard(election, viewModel) {
                        navController.navigate(Routes.adminResults(election.id.toString()))
                    }
                }
            }
        }
    }
}

@Composable
fun ElectionSelectCard(
    election: com.example.s_vote.ElectionStatus, 
    viewModel: AdminViewModel,
    onClick: () -> Unit
) {
    var showStartDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- DIALOGS ---
    if (showStartDialog) {
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text("Start Election?") },
            text = { Text("Are you sure you want to start this election now? The start time will be updated to now and the duration will be maintained.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateElectionStatus(election.id, "ACTIVE")
                    showStartDialog = false 
                }) { Text("START", color = Success) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End Election?") },
            text = { Text("This will stop all voting and notify all candidates and students. Are you sure?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateElectionStatus(election.id, "CLOSED")
                    showEndDialog = false 
                }) { Text("END", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Election?") },
            text = { Text("This will hide the election from students. You can still see it in the list.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateElectionStatus(election.id, "CANCELLED")
                    showCancelDialog = false 
                }) { Text("CANCEL", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("BACK") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Election?") },
            text = { Text("WARNING: This will permanently delete this election and ALL its votes. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteElection(election.id)
                    showDeleteDialog = false 
                }) { Text("DELETE", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") }
            }
        )
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.HowToVote, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        election.title.uppercase(), 
                        style = MaterialTheme.typography.labelLarge, 
                        fontWeight = FontWeight.Black, 
                        color = TextPrimary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
                
                if (election.startDate != null) {
                    Text(
                        "Starts: ${election.startDate}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = TextSecondary
                    )
                }
                if (election.endDate != null) {
                    Text(
                        "Ends: ${election.endDate}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = TextSecondary
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Badge for status
                val statusColor = when (election.status) {
                    "ACTIVE" -> Success
                    "UPCOMING" -> Primary
                    else -> TextMuted
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        election.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (election.status == "UPCOMING") {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showStartDialog = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("START ELECTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL ELECTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                } else if (election.status == "ACTIVE") {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showEndDialog = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("END ELECTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
