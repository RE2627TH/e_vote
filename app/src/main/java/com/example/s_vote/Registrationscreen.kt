package com.example.s_vote

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.R
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.RegisterState
import com.example.s_vote.viewmodel.RegisterViewModel
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel()

    // UI States
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Student") }
    var expanded by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        when(registerState) {
            is RegisterState.Success -> {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()
                val userId = (registerState as RegisterState.Success).userId
                if (selectedRole.equals("Candidate", ignoreCase = true) && userId != null) {
                    navController.navigate("${Routes.CANDIDATE_APPLICATION.replace("{userId}", userId.toString())}")
                } else {
                    navController.navigate(Routes.LOGIN)
                }
                viewModel.resetState()
            }
            is RegisterState.Error -> {
                Toast.makeText(context, (registerState as RegisterState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(y = (-100).dp, x = (-100).dp)
                .background(Secondary.copy(alpha = 0.05f), CircleShape)
                .align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Back Button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back", 
                    tint = TextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, OutlineColor, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_thumb_up),
                        contentDescription = "Logo",
                        modifier = Modifier.size(44.dp),
                        colorFilter = ColorFilter.tint(Primary)
                    )
                }

                Spacer(Modifier.height(24.dp))
                
                Text(
                    "JOIN E-VOTE",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 4.sp
                )
                
                Text(
                    "Create your secure account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(32.dp))

                // Inputs
                ModernRegTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                Spacer(Modifier.height(16.dp))

                // DOB Picker
                val calendar = java.util.Calendar.getInstance()
                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, y, m, d -> dob = "$y-${m + 1}-$d" },
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )

                Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                    ModernRegTextField(
                        value = dob,
                        onValueChange = { },
                        label = "Date of Birth (YYYY-MM-DD)",
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                contentDescription = null,
                                tint = Secondary.copy(alpha = 0.5f)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
                ModernRegTextField(value = idNumber, onValueChange = { idNumber = it }, label = "Registration / ID Number")
                Spacer(Modifier.height(16.dp))
                ModernRegTextField(value = department, onValueChange = { department = it }, label = "Department")
                Spacer(Modifier.height(16.dp))
                ModernRegTextField(value = email, onValueChange = { email = it }, label = "Official Email")
                Spacer(Modifier.height(16.dp))
                ModernRegTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

                Spacer(Modifier.height(16.dp))

                // Role Dropdown (Modern Styling)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ROLE") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceLight)
                    ) {
                        DropdownMenuItem(
                            text = { Text("STUDENT", color = TextPrimary, style = MaterialTheme.typography.labelLarge) },
                            onClick = { selectedRole = "Student"; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("CANDIDATE", color = TextPrimary, style = MaterialTheme.typography.labelLarge) },
                            onClick = { selectedRole = "Candidate"; expanded = false }
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Register Button
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && idNumber.isNotBlank()) {
                            viewModel.register(name, dob, idNumber, department, email, password, selectedRole.lowercase())
                        } else {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIGN UP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ModernRegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(label, color = TextSecondary.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextSecondary,
            focusedContainerColor = SurfaceLight.copy(alpha = 0.5f),
            unfocusedContainerColor = SurfaceLight.copy(alpha = 0.3f),
            disabledContainerColor = SurfaceVariant,
            focusedBorderColor = Primary,
            unfocusedBorderColor = OutlineColor,
            disabledBorderColor = OutlineColor,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextSecondary
        )
    )
}