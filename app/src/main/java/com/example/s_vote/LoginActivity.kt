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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val viewModel: LoginViewModel = viewModel()

    // UI States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(LoginActivityRole.Student) }
    
    // Animation States
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Listen to ViewModel State
    val loginState by viewModel.loginState.collectAsState()

    // Handle Navigation based on State
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val role = (loginState as LoginState.Success).role
                Toast.makeText(context, "Login Successful! Role: $role", Toast.LENGTH_SHORT).show() 

                val normalizedRole = role.trim().lowercase()

                when {
                    normalizedRole == "admin" -> {
                        navController.navigate(Routes.ADMIN_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                    normalizedRole == "candidate" -> {
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
                val errorMsg = (loginState as LoginState.Error).message
                Toast.makeText(context, errorMsg.toString(), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0533),
            Color(0xFF2104A1),
            Color(0xFF6743FF)
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glows
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = (-100).dp, x = (-100).dp)
                    .background(Color(0xFFFF3DA6).copy(alpha = 0.1f), CircleShape)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                // Logo Glass Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_thumb_up),
                        contentDescription = "Logo",
                        modifier = Modifier.size(56.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "E-VOTE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Secure & Verified Election Access",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(48.dp))

                // ROLE TOGGLE
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.height(56.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        RoleButton("STUDENT", selectedRole == LoginActivityRole.Student, Modifier.weight(1f)) {
                            selectedRole = LoginActivityRole.Student
                        }
                        RoleButton("CANDIDATE", selectedRole == LoginActivityRole.Candidate, Modifier.weight(1f)) {
                            selectedRole = LoginActivityRole.Candidate
                        }
                        RoleButton("ADMIN", selectedRole == LoginActivityRole.Admin, Modifier.weight(1f)) {
                            selectedRole = LoginActivityRole.Admin
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // INPUTS
                ModernTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email ID"
                )

                Spacer(Modifier.height(16.dp))

                ModernTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )

                // FORGOT PASSWORD
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { navController.navigate(Routes.FORGOT_PASSWORD) }) {
                        Text(
                            "Forgot Password?", 
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

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
                        .height(60.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6743FF),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                     if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIGN IN", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // REGISTER
                OutlinedButton(
                    onClick = { navController.navigate(Routes.REGISTRATION) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text("CREATE ACCOUNT", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun RoleButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.4f)
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            color = contentColor, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ModernTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.White.copy(alpha = 0.3f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
            focusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
        )
    )
}