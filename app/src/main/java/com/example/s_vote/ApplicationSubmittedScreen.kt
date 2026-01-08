package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes

@Composable
fun ApplicationSubmittedScreen(navController: NavController, applicationId: String) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(20.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Application Submitted!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Your application to become a candidate has been successfully submitted.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(Modifier.height(20.dp))

                // Application ID Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Application ID:", fontWeight = FontWeight.SemiBold)
                    Text(applicationId, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Your application is now pending review by the admin.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // View Status Button
            Button(
                onClick = {
                    navController.navigate(Routes.applicationStatus(applicationId))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("View Status", color = Color.White)
            }

            Spacer(Modifier.width(16.dp))

            // Go Home Button
            Button(
                onClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF03A9F4)
                )
            ) {
                Text("Go to Login", color = Color.White)
            }
        }
    }
}