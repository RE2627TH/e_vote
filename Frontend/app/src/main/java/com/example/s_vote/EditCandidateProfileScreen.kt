package com.example.s_vote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.AppUser
import com.example.s_vote.ui.theme.*
import com.example.s_vote.api.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCandidateProfileScreen(navController: NavController, candidateId: String) {

    val context = LocalContext.current
    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State holders
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var pledges by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var symbolPath by remember { mutableStateOf<String?>(null) }
    var symbolName by remember { mutableStateOf("") }

    // Fetch and sync data
    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    LaunchedEffect(profile) {
        profile?.let { user ->
            name = user.name
            user.candidateDetails?.let { details ->
                course = details.course ?: ""
                college = details.college ?: ""
                goals = details.goals ?: ""
                pledges = details.pledges ?: ""
                photoPath = details.photo
                symbolPath = details.symbolUrl
                symbolName = details.symbolName ?: ""
            }
        }
    }

    // Image Upload Logic
    val contentResolver = context.contentResolver

    fun createMultipartBody(uri: Uri, paramName: String): MultipartBody.Part? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(paramName, "image_${System.currentTimeMillis()}.jpg", requestFile)
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val part = createMultipartBody(it, "image")
            part?.let { p ->
                viewModel.uploadImageFile(p) { serverPath ->
                    if (serverPath != null) photoPath = serverPath
                }
            }
        }
    }

    val symbolLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val part = createMultipartBody(it, "image")
            part?.let { p ->
                viewModel.uploadImageFile(p) { serverPath ->
                    if (serverPath != null) symbolPath = serverPath
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EDIT PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PHOTO PICKERS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Photo Picker
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Profile Photo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                            .border(1.dp, if (photoPath != null) Primary else OutlineColor, CircleShape)
                            .clickable { photoLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoPath != null) {
                            val url = if (photoPath!!.startsWith("http")) photoPath else "${ApiClient.BASE_URL}$photoPath"
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text(" Tap ", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Symbol Picker
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Election Symbol", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceLight)
                            .border(1.dp, if (symbolPath != null) Primary else OutlineColor, RoundedCornerShape(16.dp))
                            .clickable { symbolLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (symbolPath != null) {
                            val url = if (symbolPath!!.startsWith("http")) symbolPath else "${ApiClient.BASE_URL}$symbolPath"
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            Text(" Tap ", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- FORM FIELDS ---
            EditField(value = name, onValueChange = { name = it }, label = "Full Name")
            EditField(value = course, onValueChange = { course = it }, label = "Department / Course")
            EditField(value = college, onValueChange = { college = it }, label = "College Name")
            EditField(value = symbolName, onValueChange = { symbolName = it }, label = "Symbol Name (e.g. Lotus, Tiger)")
            
            Spacer(Modifier.height(16.dp))
            
            EditField(value = goals, onValueChange = { goals = it }, label = "Campaign Goals", singleLine = false, height = 120.dp)
            EditField(value = pledges, onValueChange = { pledges = it }, label = "My Pledges", singleLine = false, height = 120.dp)

            Spacer(Modifier.height(40.dp))

            // --- ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, OutlineColor)
                ) {
                    Text("CANCEL", color = TextPrimary, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = {
                        viewModel.updateProfile(candidateId, name, course, college, "", goals, pledges, symbolName, photoPath, symbolPath)
                        android.widget.Toast.makeText(context, "Profile Updated!", android.widget.Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("UPDATE", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    height: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().then(if (height != androidx.compose.ui.unit.Dp.Unspecified) Modifier.height(height) else Modifier),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineColor,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight
            ),
            singleLine = singleLine
        )
    }
}
