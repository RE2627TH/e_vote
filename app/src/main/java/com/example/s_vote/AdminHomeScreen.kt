
package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.random.Random

//--------------------------------------------------------
// FILTER BUTTON
//--------------------------------------------------------
@Composable
fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {

    val bgColor = if (isSelected) Color(0xFF3E1F7F) else Color(0xFFE8E2FF)
    val textColor = if (isSelected) Color.White else Color(0xFF3E1F7F)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, fontSize = 14.sp)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("EXPERIMENTAL_API_USAGE")
@Composable
fun AdminHomeScreen(navController: NavController) {

    val viewModel: com.example.s_vote.viewmodel.AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val candidateList by viewModel.pendingCandidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // NOTE: filtering might need adjustment as API fetches only pending or approved by default.
    // Ideally we fetch ALL for admin or have tabs. For now, let's assume this screen manages ALL (which means we might need a get_all API or similar).
    // Given the previous setup, let's focus on PENDING for approval workflow.

    // To show all, we might need multiple fetches or a unified API.
    // Let's stick to showing what the viewModel provides (which currently is pending).

    LaunchedEffect(Unit) {
        viewModel.fetchPendingCandidates()
    }

    // Toast for messages
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(message) {
        message?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    var query by remember { mutableStateOf("") }
    var showDetailFor by remember { mutableStateOf<com.example.s_vote.model.Candidate?>(null) }
    var showAddDialog by remember { mutableStateOf(false) } // Removed logic for now as adding is done via registration
    var refreshing by remember { mutableStateOf(false) }

    // Filter Logic (Client side for now)
    val filteredList = candidateList.filter { c ->
        (selectedFilter == "All" || c.status.equals(selectedFilter, ignoreCase = true)) &&
                (query.isBlank() || (c.name ?: "").contains(query, ignoreCase = true) || (c.role ?: "").contains(query, ignoreCase = true))
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Candidates") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.fetchPendingCandidates()
                    }) {
                        Text(if (isLoading) "Loading..." else "Refresh", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // Add Candidate manually not supported in this flow, usually done by user registration
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(10.dp)
        ) {

            // Header
            Text(
                "Pending Requests", // Updated title to reflect context
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF3E1F7F)
            )

            Spacer(Modifier.height(12.dp))

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or role") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // List
            if (isLoading && candidateList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (candidateList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending candidates found.")
                }
            } else {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredList, key = { it.id ?: kotlin.random.Random.nextInt().toString() }) { candidate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDetailFor = candidate }
                        ) {
                            // Reusing Row UI but mapping Candidate model
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(candidate.name ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(candidate.position ?: "No Position", color = Color.Gray, fontSize = 14.sp)
                                    Text("Status: ${candidate.status ?: "Pending"}", color = if(candidate.status == "approved") Color.Green else Color.Blue, fontSize = 12.sp)
                                }

                                // Action Buttons
                                Row {
                                    Button(
                                        onClick = { viewModel.approveCandidate(candidate.id ?: "") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Approve", fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.rejectCandidate(candidate.id ?: "") },
                                        modifier = Modifier.height(36.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("Reject", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
    showDetailFor?.let { cand ->
        AlertDialog(
            onDismissRequest = { showDetailFor = null },
            title = { Text(cand.name ?: "Unknown") },
            text = {
                Column {
                    Text("Details:")
                    Text("Applied for: ${cand.position ?: "N/A"}")
                    Text("Role: ${cand.role ?: "N/A"}")
                    Text("Manifesto: ${cand.manifesto ?: "N/A"}")
                    Text("Status: ${cand.status ?: "N/A"}")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.approveCandidate(cand.id ?: ""); showDetailFor = null }) {
                    Text("Approve")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.rejectCandidate(cand.id ?: ""); showDetailFor = null }) {
                    Text("Reject")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = Color.Black)
            Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF3E1F7F))
        }
    }
}

@Composable
fun CandidateDetailDialog(candidate: com.example.s_vote.model.Candidate, onDismiss: () -> Unit, onChangeStatus: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(candidate.name ?: "Unknown") }, text = {
        Column {
            Text("Role: ${candidate.role ?: "Unknown"}")
            Spacer(Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Button(onClick = { onChangeStatus("Approved") }) { Text("Approve") }
                Button(onClick = { onChangeStatus("Pending") }) { Text("Pending") }
                OutlinedButton(onClick = { onChangeStatus("Rejected") }) { Text("Reject") }
            }
        }
    }, confirmButton = {
        TextButton(onClick = onDismiss) { Text("Close") }
    })
}

@Composable
fun AddCandidateDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Candidate") }, text = {
        Column {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") })
        }
    }, confirmButton = {
        TextButton(onClick = { if (name.isNotBlank() && role.isNotBlank()) onAdd(name, role) }) { Text("Add") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}



