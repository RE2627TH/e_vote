package com.example.s_vote

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch
import com.example.s_vote.ui.theme.*

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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEEF2FF),
            Color(0xFFE0E7FF),
            Color(0xFFF1F5F9)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MY ACCOUNT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            // Background Glows
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = (-50).dp, x = 200.dp)
                    .background(Color(0xFFFF3DA6).copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.TopEnd)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Profile Header Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(SurfaceLight)
                            .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundLight)
                                    .border(2.dp, Primary, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (name.isNotEmpty()) name.take(1).uppercase() else "U",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                name.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                studentId.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "PERSONAL INFORMATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ModernProfileField(label = "FULL NAME", value = name, onValueChange = { name = it })
                    ModernProfileField(label = "DEPARTMENT", value = department, onValueChange = { department = it })
                    
                    // DOB Picker
                    val calendar = java.util.Calendar.getInstance()
                    val datePickerDialog = android.app.DatePickerDialog(
                        context,
                        { _, y, m, d -> dob = "$y-${m + 1}-$d" },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    
                    Column(modifier = Modifier.padding(bottom = 20.dp)) {
                        Text(
                            "DATE OF BIRTH", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceLight)
                                .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (dob.isEmpty()) "Select Date" else dob,
                                    color = if (dob.isEmpty()) TextMuted else TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    ModernProfileField(label = "OFFICIAL EMAIL", value = email, onValueChange = { email = it })
                    ModernProfileField(label = "STUDENT ID / ROLL NO.", value = studentId, onValueChange = { studentId = it })
                    ModernProfileField(label = "COLLEGE / INSTITUTION", value = college, onValueChange = { college = it })

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
                            .height(60.dp)
                            .clip(RoundedCornerShape(30.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("SAVE & UPDATE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ModernProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Primary.copy(alpha = 0.1f)
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}
