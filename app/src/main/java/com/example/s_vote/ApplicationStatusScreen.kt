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
import com.example.s_vote.ui.theme.*
import androidx.compose.foundation.border

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
                    .background(BackgroundLight)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Status icon and color
                val (icon, color, statusText) = when (status) {
                    "approved" -> Triple(Icons.Default.CheckCircle, Success, "APPROVED ✓")
                    "rejected" -> Triple(Icons.Default.Error, Error, "REJECTED")
                    else -> Triple(Icons.Default.Schedule, Warning, "PENDING")
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = statusText,
                    modifier = Modifier.size(80.dp),
                    tint = color
                )
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    text = statusText, 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black, 
                    color = color,
                    letterSpacing = 2.sp
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    message, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary, 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                
                if (status == "rejected" && rejection != null) {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, Error.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("REJECTION REASON", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Error)
                            Spacer(Modifier.height(8.dp))
                            Text(rejection, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                if (status == "approved") {
                    Button(
                        onClick = { navController.navigate(Routes.LOGIN) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success
                        )
                    ) {
                        Text("LOGIN AS CANDIDATE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                } else {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("BACK", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
