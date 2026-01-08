package com.example.s_vote

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
// 👇 THESE IMPORTS ARE CRITICAL - DO NOT REMOVE
import com.example.s_vote.viewmodel.LoginViewModel
import com.example.s_vote.viewmodel.LoginState

// Role Enum (UI purpose only - Backend decides actual role)
enum class LoginActivityRole { Student, Candidate, Admin }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    // 👇 Explicitly specifying the type fixes the "Not enough information" error
    val viewModel: LoginViewModel = viewModel()



    // UI States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(LoginActivityRole.Student) }

    // Listen to ViewModel State
    val loginState by viewModel.loginState.collectAsState()

    // Handle Navigation based on State
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val role = (loginState as LoginState.Success).role
                Toast.makeText(context, "Login Successful! Role: $role", Toast.LENGTH_SHORT).show() // Debug toast

                // Backend Role padi Navigation logic
                val normalizedRole = role.trim().lowercase()

                when {
                    normalizedRole == "admin" -> {
                        navController.navigate(Routes.ADMIN_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                    normalizedRole == "candidate" -> {
                        // Get stored ID from SharedPreferences
                        val sharedPref = context.getSharedPreferences("s_vote_prefs", Context.MODE_PRIVATE)
                        val userId = sharedPref.getString("USER_ID", "1") ?: "1"

                        navController.navigate("candidate_dashboard/$userId") {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                    else -> { // Default to Student Home
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
                viewModel.resetState()
            }
            is LoginState.Error -> {
                // 👇 Explicit String cast fixes "Overload resolution ambiguity"
                val errorMsg = (loginState as LoginState.Error).message
                Toast.makeText(context, errorMsg.toString(), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(20.dp))

        // LOGO
        Image(
            painter = painterResource(R.drawable.ic_thumb_up), // Ensure this drawable exists
            contentDescription = "Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF30216E))
                .padding(16.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text("E-Vote", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Secure & verified Election", fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        // ROLE TOGGLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoleButton("Student", selectedRole == LoginActivityRole.Student, Modifier.weight(1f)) {
                selectedRole = LoginActivityRole.Student
            }
            RoleButton("Candidate", selectedRole == LoginActivityRole.Candidate, Modifier.weight(1f)) {
                selectedRole = LoginActivityRole.Candidate
            }
            RoleButton("Admin", selectedRole == LoginActivityRole.Admin, Modifier.weight(1f)) {
                selectedRole = LoginActivityRole.Admin
            }
        }

        Spacer(Modifier.height(20.dp))

        // EMAIL INPUT
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // PASSWORD INPUT
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // SIGN IN BUTTON
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF220385),
                contentColor = Color.White
            )
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // REGISTER BUTTON
        OutlinedButton(
            onClick = {
                navController.navigate(Routes.REGISTRATION)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF220385)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
        ) {
            Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        // FORGOT PASSWORD
        TextButton(
            onClick = {
                navController.navigate(Routes.FORGOT_PASSWORD)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Forgot Password?",
                color = Color(0xFF220385),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Error Message Display
        if (loginState is LoginState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = (loginState as LoginState.Error).message,
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun RoleButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (selected) Color(0xFFEDE7FF) else Color.White
    val textColor = if (selected) Color(0xFF3E1F7F) else Color.Gray

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Medium)
    }
}