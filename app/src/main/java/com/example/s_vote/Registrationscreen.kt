package com.example.s_vote

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.R
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.RegisterState
import com.example.s_vote.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel()

    // UI State for all fields from image design
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // Date of Birth
    var idNumber by remember { mutableStateOf("") } // Student/ID Number
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF30216E))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo from image design
            Image(
                painter = painterResource(id = R.drawable.ic_thumb_up), // Replace with your logo resource
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF30216E))
                    .padding(12.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("Register Now!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(24.dp))

            // Input Fields matching image_9d11a6.png
            RegistrationTextField(value = name, onValueChange = { name = it }, label = "Name")
            Spacer(Modifier.height(12.dp))

            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    dob = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                }, year, month, day
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { },
                label = { Text("Date of Birth (YYYY-MM-DD)", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }, // Make the whole field clickable
                enabled = false, // Disable manual typing
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_menu_my_calendar), contentDescription = "Select Date")
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            RegistrationTextField(value = idNumber, onValueChange = { idNumber = it }, label = "ID Number")
            Spacer(Modifier.height(12.dp))

            RegistrationTextField(value = department, onValueChange = { department = it }, label = "Department")
            Spacer(Modifier.height(12.dp))

            RegistrationTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(Modifier.height(12.dp))

            RegistrationTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

            Spacer(Modifier.height(12.dp))

            // Role Selector matching design
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Student") },
                        onClick = { selectedRole = "Student"; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Candidate") },
                        onClick = { selectedRole = "Candidate"; expanded = false }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && idNumber.isNotBlank()) {
                        // Backend requires extra fields now based on image_9ac40b.png
                        viewModel.register(
                            name = name,
                            dob = dob,
                            student_id = idNumber,
                            department = department,
                            email = email,
                            password = password,
                            role = selectedRole.lowercase()
                        )
                    } else {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADC2FF)) // Match button color in image
            ) {
                if (registerState is RegisterState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30216E))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}