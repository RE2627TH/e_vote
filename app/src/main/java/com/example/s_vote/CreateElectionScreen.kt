package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateElectionScreen(navController: NavController) {

    var electionName by remember { mutableStateOf("") }
    var startingDate by remember { mutableStateOf("") }
    var endingDate by remember { mutableStateOf("") }
    var candidates by remember { mutableStateOf("") }

    val viewModel: com.example.s_vote.viewmodel.AdminViewModel = viewModel()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
            if(it.contains("created", ignoreCase = true)) {
                 navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Election") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2104A1),
                    titleContentColor = Color.White
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Create election", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = electionName,
                onValueChange = { electionName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                label = { Text("Election Title") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Starting Date", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = startingDate,
                onValueChange = { startingDate = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD HH:MM:SS") },
                label = { Text("Start Date") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Ending date", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = endingDate,
                onValueChange = { endingDate = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD HH:MM:SS") },
                label = { Text("End Date") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Candidates are managed via Admin Home now, so this field is maybe just info or disabled
            Text("Add candidates via Manage Candidates Screen", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        if(electionName.isNotEmpty() && startingDate.isNotEmpty() && endingDate.isNotEmpty()) {
                            viewModel.createElection(electionName, startingDate, endingDate)
                        } 
                    }, 
                    colors = ButtonDefaults.buttonColors(Color(0xFF6743FF)),
                    enabled = !isLoading
                ) {
                    Text("Create Election")
                }
                
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(Color.Gray)) {
                    Text("Cancel")
                }
            }
        }
    }
}
