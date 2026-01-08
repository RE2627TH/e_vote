package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF18048A),
                    titleContentColor = Color.White
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {
            ProfileTextField(label = "Name", value = name, onValueChange = { name = it })
            ProfileTextField(label = "Department", value = department, onValueChange = { department = it })
            ProfileTextField(label = "Date of Birth", value = dob, onValueChange = { dob = it })
            ProfileTextField(label = "Email ID", value = email, onValueChange = { email = it })
            ProfileTextField(label = "mobile No", value = mobile, onValueChange = { mobile = it })
            ProfileTextField(label = "Student ID", value = studentId, onValueChange = { studentId = it })

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF))
            ) {
                Text("save & update")
            }
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Text(text = label, fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}
