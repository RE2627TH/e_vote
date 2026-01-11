package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get User ID from SharedPreferences
    val sharedPref = context.getSharedPreferences("s_vote_prefs", android.content.Context.MODE_PRIVATE)
    val userId = sharedPref.getString("USER_ID", "") ?: ""

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }

    // Fetch Profile Data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            try {
                val response = com.example.s_vote.api.RetrofitInstance.api.getProfile(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!.user
                    name = user.name
                    email = user.email
                    department = user.department ?: ""
                    dob = user.dob ?: ""
                    studentId = user.studentId ?: ""
                    college = user.college ?: ""
                } else {
                    android.widget.Toast.makeText(context, "Failed to load profile", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
                    .padding(16.dp)
            ) {
                ProfileTextField(label = "Name", value = name, onValueChange = { name = it })
                ProfileTextField(label = "Department", value = department, onValueChange = { department = it })
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
                
                Text(text = "Date of Birth", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = dob,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    trailingIcon = {
                         IconButton(onClick = { datePickerDialog.show() }) {
                             Icon(painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_my_calendar), contentDescription = "Select Date")
                         }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                ProfileTextField(label = "Email ID", value = email, onValueChange = { email = it })
                ProfileTextField(label = "Student ID", value = studentId, onValueChange = { studentId = it })
                ProfileTextField(label = "College Name", value = college, onValueChange = { college = it })

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val request = com.example.s_vote.model.UpdateUserProfileRequest(
                                    userId = userId,
                                    name = name,
                                    department = department,
                                    dob = dob,
                                    email = email,
                                    studentId = studentId,
                                    college = college
                                )
                                val response = com.example.s_vote.api.RetrofitInstance.api.updateUserProfile(request)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    android.widget.Toast.makeText(context, "Profile Updated Successfully", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Update Failed: ${response.body()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("save & update")
                    }
                }
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
