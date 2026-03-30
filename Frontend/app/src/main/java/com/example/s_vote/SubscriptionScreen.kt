package com.example.s_vote

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.viewmodel.SubscriptionState
import com.example.s_vote.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionScreen(navController: NavController, userId: String, role: String) {
    val context = LocalContext.current
    val viewModel: SubscriptionViewModel = viewModel()
    val state by viewModel.subscriptionState.collectAsState()

    LaunchedEffect(state) {
        if (state is SubscriptionState.Success) {
            Toast.makeText(context, "Payment Successful! Please login to continue.", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        } else if (state is SubscriptionState.Error) {
            Toast.makeText(context, (state as SubscriptionState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "ACTIVATE YOUR ACCOUNT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Complete a one-time payment to unlock lifetime access to the E-Vote platform.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // Pricing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "₹100",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Primary
                    )
                    Text(
                        "ONE-TIME PAYMENT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(24.dp))

                    Divider(color = OutlineColor)

                    Spacer(Modifier.height(24.dp))

                    BenefitItem("Secure Digital Voting")
                    BenefitItem("Real-time Election Updates")
                    BenefitItem("Candidate Manifestos")
                    BenefitItem("Lifetime Account Validity")
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { viewModel.completePayment(userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = state !is SubscriptionState.Loading
            ) {
                if (state is SubscriptionState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PAY ₹100 & CONTINUE", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
