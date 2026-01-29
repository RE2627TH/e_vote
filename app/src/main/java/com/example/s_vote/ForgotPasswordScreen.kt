package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.s_vote.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: com.example.s_vote.viewmodel.ForgotPasswordViewModel = viewModel()
    val resetState by viewModel.resetState.collectAsState()

    var id by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(resetState) {
        when(resetState) {
            is com.example.s_vote.viewmodel.ResetState.Success -> {
                android.widget.Toast.makeText(context, (resetState as com.example.s_vote.viewmodel.ResetState.Success).message, android.widget.Toast.LENGTH_LONG).show()
                navController.popBackStack() 
                viewModel.resetState()
            }
            is com.example.s_vote.viewmodel.ResetState.Error -> {
                android.widget.Toast.makeText(context, (resetState as com.example.s_vote.viewmodel.ResetState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BackgroundLight,
            SurfaceLight
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "RESET ACCESS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glows
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = (-100).dp, x = 200.dp)
                    .background(Primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Account Recovery",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Authorize Identity to Reset Password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Modern Input Fields
                ModernForgotField(label = "STUDENT ID / ROLL NO.", value = id, onValueChange = { id = it })
                ModernForgotField(label = "REGISTERED EMAIL", value = email, onValueChange = { email = it })
                ModernForgotField(label = "NEW PASSWORD", value = password, onValueChange = { password = it }, isPassword = true)
                ModernForgotField(label = "CONFIRM NEW PASSWORD", value = confirmPassword, onValueChange = { confirmPassword = it }, isPassword = true)

                Spacer(modifier = Modifier.height(32.dp))

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
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = resetState !is com.example.s_vote.viewmodel.ResetState.Loading,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (resetState is com.example.s_vote.viewmodel.ResetState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SUBMIT REQUEST", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernForgotField(label: String, value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceLight.copy(alpha = 0.5f),
                unfocusedContainerColor = SurfaceLight.copy(alpha = 0.3f),
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineColor,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary
            )
        )
    }
}
