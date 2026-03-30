package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*

@Composable
fun ApplicationSubmittedScreen(navController: NavController, applicationId: String) {

// Background is now handled by the Scaffold containerColor

    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceLight
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SuccessMild.copy(alpha = 0.1f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessMild,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "APPLICATION SUBMITTED",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Your application to become a candidate has been successfully submitted.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )

                Spacer(Modifier.height(24.dp))

                // Application ID Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("APPLICATION ID", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(applicationId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Your application is now pending review by the admin unit.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // View Status Button
            Button(
                onClick = {
                    navController.navigate(Routes.applicationStatus(applicationId))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text("VIEW STATUS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Go Home Button
            TextButton(
                onClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("BACK TO LOGIN", color = TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
}