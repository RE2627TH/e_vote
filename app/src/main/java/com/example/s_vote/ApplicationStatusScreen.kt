package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.CandidateApplicationViewModel

@Composable
fun ApplicationStatusScreen(navController: NavController, applicationId: String) {
    val viewModel: CandidateApplicationViewModel = viewModel()
    val statusResponse = viewModel.statusResponse.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkApplicationStatusById(applicationId.toInt())
    }
    
    when {
        isLoading.value -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        statusResponse.value != null -> {
            val status = statusResponse.value!!.application?.status
            val message = statusResponse.value!!.message
            val rejection = statusResponse.value!!.reason
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Status icon and color
                val (icon, color, statusText) = when (status) {
                    "approved" -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), "APPROVED ✓")
                    "rejected" -> Triple(Icons.Default.Error, Color(0xFFFF6B6B), "REJECTED")
                    else -> Triple(Icons.Default.Schedule, Color(0xFFFFA500), "PENDING")
                }
                
                Icon(
                    icon,
                    contentDescription = statusText,
                    modifier = Modifier.size(80.dp),
                    tint = color
                )
                
                Spacer(Modifier.height(20.dp))
                
                Text(statusText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                
                Spacer(Modifier.height(16.dp))
                
                Text(message, fontSize = 16.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(0.9f))
                
                if (status == "rejected" && rejection != null) {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Reason for Rejection:", fontWeight = FontWeight.Bold, color = Color.Red)
                            Spacer(Modifier.height(8.dp))
                            Text(rejection, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                if (status == "approved") {
                    Button(
                        onClick = { navController.navigate(Routes.LOGIN) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Login as Candidate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp)
                    ) {
                        Text("Back", fontSize = 16.sp)
                    }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to load status")
            }
        }
    }
}
