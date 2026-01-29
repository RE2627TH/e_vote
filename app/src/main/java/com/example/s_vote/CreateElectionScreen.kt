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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.example.s_vote.ui.theme.*


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
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("CREATE ELECTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = electionName,
                onValueChange = { electionName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter Election Title...", color = TextMuted) },
                label = { Text("ELECTION TITLE") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.1f),
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("STARTING DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = startingDate,
                onValueChange = { startingDate = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD HH:MM:SS", color = TextMuted) },
                label = { Text("START DATE") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.1f),
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("ENDING DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = endingDate,
                onValueChange = { endingDate = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD HH:MM:SS", color = TextMuted) },
                label = { Text("END DATE") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.1f),
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Candidates are managed via Admin Home now, so this field is maybe just info or disabled
            Text("Add candidates via Manage Candidates Screen", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        if(electionName.isNotEmpty() && startingDate.isNotEmpty() && endingDate.isNotEmpty()) {
                            viewModel.createElection(electionName, startingDate, endingDate)
                        } 
                    }, 
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(Primary),
                    enabled = !isLoading
                ) {
                    Text("CREATE NOW", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                
                Button(
                    onClick = { navController.popBackStack() }, 
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f))
                ) {
                    Text("CANCEL", color = Primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
    }
}
