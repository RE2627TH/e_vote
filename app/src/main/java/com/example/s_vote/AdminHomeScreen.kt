
package com.example.s_vote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.s_vote.ui.theme.*

//--------------------------------------------------------
// FILTER BUTTON
//--------------------------------------------------------
@Composable
fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {

    val containerColor = if (isSelected) Primary else SurfaceLight
    val contentColor = if (isSelected) Color.White else TextPrimary

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f)),
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Text(
            label.uppercase(), 
            color = contentColor, 
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("EXPERIMENTAL_API_USAGE")
@Composable
fun AdminHomeScreen(navController: NavController) {

    val viewModel: com.example.s_vote.viewmodel.AdminViewModel = viewModel()
    val candidateList by viewModel.pendingCandidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } 
    
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
    var showAddDialog by remember { mutableStateOf(false) } 
    var refreshing by remember { mutableStateOf(false) }

    // Filter Logic
    val filteredList = candidateList.filter { c ->
        (selectedFilter == "All" || c.status.equals(selectedFilter, ignoreCase = true)) &&
                (query.isBlank() || (c.name ?: "").contains(query, ignoreCase = true) || (c.role ?: "").contains(query, ignoreCase = true))
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "MANAGE CANDIDATES", 
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
                actions = {
                    TextButton(onClick = { viewModel.fetchPendingCandidates() }) {
                        Text(
                            if (isLoading) "SYNCING..." else "SYNC LIST", 
                            color = Primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // Header
            Text(
                "PENDING VERIFICATION", 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(16.dp))

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search candidates...", color = TextSecondary.copy(alpha=0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = {
                   Icon(painter = painterResource(android.R.drawable.ic_menu_search), contentDescription = "Search", tint = Primary) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.1f)
                )
            )

            Spacer(Modifier.height(16.dp))

            // List
            if (isLoading && candidateList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (candidateList.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No pending approvals", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredList, key = { it.id ?: kotlin.random.Random.nextInt().toString() }) { candidate ->
                        
                        // User Card
                        Card(
                            onClick = { showDetailFor = candidate },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Placeholder
                                Surface(
                                    shape = CircleShape,
                                    color = BackgroundLight,
                                    modifier = Modifier.size(56.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            (candidate.name ?: "?").take(1).uppercase(), 
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Primary
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        (candidate.name ?: "Unknown").uppercase(), 
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        (candidate.position ?: "No Position").uppercase(), 
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if(candidate.status == "approved") Success.copy(alpha=0.1f) else Warning.copy(alpha=0.1f)
                                    ) {
                                        Text(
                                            (candidate.status ?: "Pending").uppercase(), 
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if(candidate.status == "approved") Success else Warning,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                // Action Buttons (Compact)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledIconButton(
                                        onClick = { viewModel.approveCandidate(candidate.id ?: "") },
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftGreen),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(painter = painterResource(android.R.drawable.ic_input_add), contentDescription = "Approve", tint = Color.White) // Should ideally be check
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledIconButton(
                                        onClick = { viewModel.rejectCandidate(candidate.id ?: "") },
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(painter = painterResource(android.R.drawable.ic_delete), contentDescription = "Reject", tint = Color.White)
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
            containerColor = SurfaceLight,
            title = { 
                Text(
                    (cand.name ?: "Candidate Details").uppercase(), 
                    style = MaterialTheme.typography.titleLarge, 
                    color = TextPrimary, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Position", cand.position ?: "N/A")
                    DetailRow("Role", cand.role ?: "N/A")
                    Text("Manifesto:", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(cand.manifesto ?: "No manifesto provided.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Current Status: ${(cand.status ?: "Pending").uppercase()}", color = Secondary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.approveCandidate(cand.id ?: ""); showDetailFor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("APPROVE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.rejectCandidate(cand.id ?: ""); showDetailFor = null }
                ) {
                    Text("REJECT", color = Error, fontWeight = FontWeight.Black)
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

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}




