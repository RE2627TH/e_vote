package com.example.s_vote

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.AdminViewModel
import com.example.s_vote.ui.theme.*
import com.example.s_vote.api.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentDetailScreen(navController: NavController, userId: String) {
    val viewModel: AdminViewModel = viewModel()
    val studentProfile by viewModel.selectedStudent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.fetchStudentDetail(userId)
    }

    // Handle Deletion Success
    val context = androidx.compose.ui.platform.LocalContext.current
    val message by viewModel.message.collectAsState()
    LaunchedEffect(message) {
        if (message?.contains("successfully", ignoreCase = true) == true) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "STUDENT DETAIL", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFB91C1C))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (studentProfile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Student details not found", color = Color.Gray)
            }
        } else {
            val user = studentProfile!!.user
            val details = user.candidateDetails

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundLight)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- HEADER SECTION ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Primary.copy(alpha = 0.1f), BackgroundLight)
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             val photoUrl = user.profilePhoto?.let {
                                 if (it.startsWith("http")) it else "${ApiClient.BASE_URL}$it"
                             }
                             Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceLight)
                                    .border(2.dp, Primary, CircleShape),
                                contentAlignment = Alignment.Center
                             ) {
                                if (photoUrl != null) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        user.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Primary
                                    )
                                }
                             }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                user.name.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (user.role == "candidate") Primary.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    user.role.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (user.role == "candidate") Primary else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // --- CONTENT SECTION ---
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        
                        Text(
                            "PERSONAL INFORMATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        DetailInfoRow(label = "STUDENT ID", value = user.studentId ?: "N/A")
                        DetailInfoRow(label = "EMAIL", value = user.email)
                        DetailInfoRow(label = "DEPARTMENT", value = user.department ?: "N/A")
                        DetailInfoRow(label = "COLLEGE", value = user.college ?: "N/A")
                        DetailInfoRow(label = "DOB", value = user.dob ?: "N/A")

                        if (user.role == "candidate" && details != null) {
                            Spacer(Modifier.height(32.dp))
                            Text(
                                "CANDIDATE INFORMATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            DetailInfoRow(label = "POSITION", value = details.position ?: "N/A")
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text("MANIFESTO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SurfaceLight,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.05f))
                            ) {
                                Text(
                                    details.manifesto ?: "No manifesto.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            
                            Text("CAMPAIGN GOALS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SurfaceLight,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.05f))
                            ) {
                                Text(
                                    details.goals ?: "No goals specified.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            
                            Text("MY PLEDGE TO STUDENTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SurfaceLight,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.05f))
                            ) {
                                Text(
                                    details.pledges ?: "No pledges specified.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(40.dp))
                    }
                }

                if (showDeleteDialog && studentProfile != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Student?", fontWeight = FontWeight.Black) },
                        text = { Text("This will permanently remove the student's profile and all their associated votes. This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteStudent(userId) {
                                        // Handled by message LaunchedEffect
                                    }
                                }
                            ) {
                                Text("DELETE", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("CANCEL", fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = BackgroundLight,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Primary.copy(alpha = 0.05f))
    }
}
