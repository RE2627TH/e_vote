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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
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
    
    // Get User ID from SessionManager
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId() ?: ""

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    profilePhoto = user.profilePhoto
                } else {
                    android.widget.Toast.makeText(context, response.body()?.message ?: "Failed to load profile", android.widget.Toast.LENGTH_SHORT).show()
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
        bottomBar = { BottomNavBar(navController, selectedRoute = com.example.s_vote.navigation.Routes.PROFILE) },
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
                                val nameInitial = if (name.isNotEmpty()) name.take(1).uppercase() else "U"
                                
                                if (!profilePhoto.isNullOrEmpty()) {
                                    val fullUrl = com.example.s_vote.api.ApiClient.BASE_URL + profilePhoto
                                    coil.compose.SubcomposeAsyncImage(
                                        model = fullUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        loading = { CircularProgressIndicator(modifier = Modifier.scale(0.5f), strokeWidth = 2.dp) },
                                        error = {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(nameInitial, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Primary)
                                            }
                                        }
                                    )
                                } else {
                                    Text(nameInitial, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Primary)
                                }
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

                    ProfileInfoRow(label = "FULL NAME", value = name)
                    ProfileInfoRow(label = "DEPARTMENT", value = department)
                    ProfileInfoRow(label = "DATE OF BIRTH", value = dob)
                    ProfileInfoRow(label = "OFFICIAL EMAIL", value = email)
                    ProfileInfoRow(label = "STUDENT ID / ROLL NO.", value = studentId)
                    ProfileInfoRow(label = "COLLEGE / INSTITUTION", value = college)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.navigate(com.example.s_vote.navigation.Routes.EDIT_PROFILE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(30.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("EDIT PROFILE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // LOGOUT BUTTON
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("SIGN OUT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Logout Confirmation Dialog
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to sign out of E-vote?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                sessionManager.logout()
                                navController.navigate(com.example.s_vote.navigation.Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        ) {
                            Text("LOGOUT", color = Error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("CANCEL", color = TextSecondary)
                        }
                    },
                    containerColor = SurfaceLight,
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLight.copy(alpha = 0.5f))
                .border(1.dp, Primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                if (value.isEmpty()) "Not Provided" else value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isEmpty()) TextMuted else TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
