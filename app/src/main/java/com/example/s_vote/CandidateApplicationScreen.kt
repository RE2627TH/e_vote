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
            .background(Color.White)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }

        Text("Candidate Application", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Fill in your details", fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(20.dp))

        @Composable
        fun field(value: String, onChange: (String) -> Unit, label: String) {

            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(12.dp))
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
                .height(120.dp),
            maxLines = 4,
            shape = RoundedCornerShape(8.dp)
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
                .height(50.dp),
            enabled = !isLoading
        ) {
            Text("Submit Application", fontWeight = FontWeight.Bold)
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
