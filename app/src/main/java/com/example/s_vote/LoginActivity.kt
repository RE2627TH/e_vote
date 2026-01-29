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
import com.example.s_vote.ui.theme.*
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
            BackgroundLight,
            SurfaceLight
        )
    )

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Background Glows - Subtler for Light Theme
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = (-150).dp, x = (-150).dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape)
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

                // Logo Container - Refined for professional look
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceLight)
                        .border(1.dp, OutlineColor, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_thumb_up),
                        contentDescription = "Logo",
                        modifier = Modifier.size(48.dp),
                        colorFilter = ColorFilter.tint(Primary)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "E-VOTE",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 4.sp
                )
                
                Text(
                    "Secure Digital Election Portal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(48.dp))

                // ROLE TOGGLE
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceLight,
                    modifier = Modifier.height(56.dp).fillMaxWidth(),
                    border = BorderStroke(1.dp, OutlineColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            color = Secondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // SIGN IN BUTTON
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(email, password, selectedRole.name)
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                     if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIGN IN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // REGISTER
                TextButton(
                    onClick = { navController.navigate(Routes.REGISTRATION) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("New here? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("Create Account", color = Secondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun RoleButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (selected) Primary else Color.Transparent
    val contentColor = if (selected) Color.White else TextSecondary
    val elevation = if (selected) 4.dp else 0.dp
    
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        shadowElevation = elevation
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text, 
                color = contentColor, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
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
        label = { Text(label) },
        placeholder = { Text(label, color = TextSecondary.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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