package com.example.s_vote

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import androidx.compose.foundation.background


@Composable
fun ScanIdScreen(navController: NavController, candidateId: String, position: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: com.example.s_vote.viewmodel.OcrViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Camera launcher (Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.processBitmap(it)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.processImage(it, context)
        }
    }

    // Handle State Changes
    LaunchedEffect(uiState) {
        when(uiState) {
            is com.example.s_vote.viewmodel.OcrState.Verified -> {
                val data = (uiState as com.example.s_vote.viewmodel.OcrState.Verified).studentData
                // Pass data to detailed screen. (Using URL encoding/json usually better but query params for simplicity)
                val verifiedRoute = Routes.voteConfirmation(
                    candidateId = candidateId,
                    position = position,
                    name = data.name,
                    dept = data.department,
                    id = data.studentId
                )
                navController.navigate(verifiedRoute)
            }
            is com.example.s_vote.viewmodel.OcrState.Error -> {
               android.widget.Toast.makeText(context, (uiState as com.example.s_vote.viewmodel.OcrState.Error).message, android.widget.Toast.LENGTH_LONG).show()
               viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, selectedRoute = Routes.HOME)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_thumb_up),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            Text("Scan Student ID Card", style = MaterialTheme.typography.headlineSmall)

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_scan),
                    contentDescription = "Scan Area",
                    modifier = Modifier.size(200.dp)
                )
                if (uiState is com.example.s_vote.viewmodel.OcrState.Scanning || uiState is com.example.s_vote.viewmodel.OcrState.Verifying) {
                    CircularProgressIndicator()
                }
            }

            Text("Ensure 'Saveetha College' text is visible")

            // Scan Button
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(22.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6743FF)
                )
            ) {
                Text("Scan ID (Camera)", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gallery Upload Button
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(22.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9370DB)
                )
            ) {
                Text("Upload from Gallery", color = Color.White)
            }
        }
    }
}
