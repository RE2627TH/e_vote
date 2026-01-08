package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: com.example.s_vote.viewmodel.ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val resetState by viewModel.resetState.collectAsState()

    var id by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Handle State
    LaunchedEffect(resetState) {
        when(resetState) {
            is com.example.s_vote.viewmodel.ResetState.Success -> {
                android.widget.Toast.makeText(context, (resetState as com.example.s_vote.viewmodel.ResetState.Success).message, android.widget.Toast.LENGTH_LONG).show()
                navController.popBackStack() // Go back to login
                viewModel.resetState()
            }
            is com.example.s_vote.viewmodel.ResetState.Error -> {
                android.widget.Toast.makeText(context, (resetState as com.example.s_vote.viewmodel.ResetState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)   // ⭐ WHITE BACKGROUND ADDED
            .padding(20.dp)
    ) {

        // Top bar with back arrow
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome Back!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Reset your password", fontSize = 16.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("ID No") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email ID") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Create Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (id.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    if (password == confirmPassword) {
                        viewModel.resetPassword(id, email, password)
                    } else {
                        android.widget.Toast.makeText(context, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF28038F)),
            enabled = resetState !is com.example.s_vote.viewmodel.ResetState.Loading
        ) {
            if (resetState is com.example.s_vote.viewmodel.ResetState.Loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Submit", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
