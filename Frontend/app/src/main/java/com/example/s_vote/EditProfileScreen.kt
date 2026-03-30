package com.example.s_vote

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = SessionManager(context)
    val userId = sessionManager.getUserId() ?: ""

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var currentPhotoUrl by remember { mutableStateOf<String?>(null) }
    
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    // Fetch Current Profile
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
                    currentPhotoUrl = user.profilePhoto
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDIT PROFILE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading && name.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(2.dp, Primary.copy(alpha = 0.1f), CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!currentPhotoUrl.isNullOrEmpty()) {
                            coil.compose.AsyncImage(
                                model = com.example.s_vote.api.ApiClient.BASE_URL + currentPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .border(2.dp, SurfaceLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Input Fields
                EditableProfileField(label = "FULL NAME", value = name) { name = it }
                EditableProfileField(label = "DEPARTMENT", value = department) { department = it }
                
                // DOB Picker (Same logic as before)
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    Text("DATE OF BIRTH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceLight).border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .clickable { 
                                val calendar = java.util.Calendar.getInstance()
                                android.app.DatePickerDialog(context, { _, y, m, d -> dob = "$y-${m + 1}-$d" }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(if (dob.isEmpty()) "Select Date" else dob, color = if (dob.isEmpty()) TextMuted else TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                EditableProfileField(label = "OFFICIAL EMAIL", value = email) { email = it }
                EditableProfileField(label = "STUDENT ID / ROLL NO.", value = studentId) { studentId = it }
                EditableProfileField(label = "COLLEGE / INSTITUTION", value = college) { college = it }

                Spacer(modifier = Modifier.height(40.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CANCEL", color = Primary)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    var base64Photo: String? = null
                                    selectedBitmap?.let {
                                        val outputStream = ByteArrayOutputStream()
                                        it.compress(Bitmap.`CompressFormat`.JPEG, 70, outputStream)
                                        base64Photo = "data:image/jpeg;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                                    }

                                    // Reuse update endpoint but with photo capability if needed
                                    // For now using existingUserProfileUpdate but we need to update model
                                    // Update backend update_profile.php to handle photo if base64 provided
                                    
                                    val request = com.example.s_vote.model.UpdateUserProfileRequest(
                                        userId = userId,
                                        name = name,
                                        department = department,
                                        dob = dob,
                                        email = email,
                                        studentId = studentId,
                                        college = college,
                                        profilePhoto = base64Photo // We need to add this to UpdateUserProfileRequest
                                    )
                                    
                                    val response = com.example.s_vote.api.RetrofitInstance.api.updateUserProfile(request)
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, response.body()?.message ?: "Update Failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("UPDATE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EditableProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Primary.copy(alpha = 0.1f)
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}
