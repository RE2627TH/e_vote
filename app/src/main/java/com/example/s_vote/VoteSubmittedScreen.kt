package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes


@Composable
fun VoteSubmittedScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, selectedRoute = Routes.RESULT)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF2105A4)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF220B91)
                )
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(30.dp))
            
            androidx.compose.material3.Text(
                "Vote Submitted Successfully!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))

            androidx.compose.material3.Button(
                onClick = { navController.navigate(Routes.RESULT) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                androidx.compose.material3.Text("View Results", color = Color(0xFF2105A4))
            }
        }
    }
}
