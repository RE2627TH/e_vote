package com.example.s_vote

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateApplicationScreen(
    navController: NavController,
    userId: Int
) {
    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel =
        viewModel()

    val scrollState = rememberScrollState()

    // Form state
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var manifesto by remember { mutableStateOf("") }

    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ✅ Handle submit success properly
    LaunchedEffect(submitSuccess) {
        submitSuccess?.let { response ->
            if (response.success) {
                val appId = response.applicationId ?: return@let

                navController.navigate(
                    Routes.applicationSubmitted(appId)
                ) {
                    popUpTo(Routes.CANDIDATE_APPLICATION) {
                        inclusive = true
                    }
                }

                viewModel.clearAllState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top bar (back allowed before submit)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
        }

        Text(
            "CANDIDATE APPLICATION", 
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 2.sp
        )
        Text(
            "Enter your professional details carefully", 
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(20.dp))

        @Composable
        fun field(value: String, onChange: (String) -> Unit, label: String) {

            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = OutlineColor,
                    focusedLabelColor = Secondary,
                    unfocusedLabelColor = TextSecondary
                )
            )
            Spacer(Modifier.height(16.dp))
        }

        field(name, { name = it }, "Full Name")
        val context = LocalContext.current
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
            label = { Text("Date of Birth") },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable { datePickerDialog.show() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextPrimary,
                disabledContainerColor = SurfaceLight,
                disabledBorderColor = OutlineColor,
                disabledLabelColor = TextSecondary,
                disabledTrailingIconColor = TextSecondary
            ),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_my_calendar), contentDescription = "Select Date", tint = Secondary)
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        field(studentId, { studentId = it }, "Student ID")
        field(email, { email = it }, "Email")
        field(phone, { phone = it }, "Phone")
        field(department, { department = it }, "Department")
        field(position, { position = it }, "Position Applying For")

        OutlinedTextField(
            value = manifesto,
            onValueChange = { manifesto = it },
            label = { Text("Manifesto / Vision") },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(150.dp),
            maxLines = 6,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedBorderColor = Secondary,
                unfocusedBorderColor = OutlineColor,
                focusedLabelColor = Secondary,
                unfocusedLabelColor = TextSecondary
            )
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && studentId.isNotBlank() && position.isNotBlank()) {
                    viewModel.submitCandidateApplication(
                        userId = userId.toString(),
                        name = name,
                        dob = dob,
                        studentId = studentId,
                        email = email,
                        phone = phone,
                        department = department,
                        position = position,
                        manifesto = manifesto
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !isLoading
        ) {
            Text(
                "SUBMIT APPLICATION", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = Color.Red)
        }

        Spacer(Modifier.height(40.dp))
    }
}
