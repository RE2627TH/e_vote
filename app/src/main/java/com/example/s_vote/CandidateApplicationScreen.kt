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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

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
    var position by remember { mutableStateOf("") }
    var manifesto by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var pledges by remember { mutableStateOf("") }
    var symbolName by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var symbolPath by remember { mutableStateOf<String?>(null) }
    var symbolUri by remember { mutableStateOf<Uri?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Image Picker Launcher
    val symbolPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            symbolUri = uri
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", "symbol_${System.currentTimeMillis()}.jpg", requestFile)
                viewModel.uploadImageFile(body) { path ->
                    symbolPath = path
                }
            }
        }
    }

    val profilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profileUri = uri
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", "candidate_${System.currentTimeMillis()}.jpg", requestFile)
                viewModel.uploadImageFile(body) { path ->
                    photoPath = path
                }
            }
        }
    }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val positions = listOf("President", "Vice President", "Sports Secretary", "Cultural Secretary", "Discipline Secretary", "Treasurer")

    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Success Navigation
    LaunchedEffect(submitSuccess) {
        submitSuccess?.let { response ->
            if (response.success) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.CANDIDATE_APPLICATION) {
                        this.inclusive = true
                    }
                }
                viewModel.clearAllState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CANDIDATE APPLICATION", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Campaign Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                "Set up your election profile and vision",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
            )

            // --- 0. Profile Photo Section (New) ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(SurfaceLight)
                    .border(2.dp, if (profileUri != null) Primary else OutlineColor, androidx.compose.foundation.shape.CircleShape)
                    .clickable { 
                        profilePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profileUri != null) {
                    AsyncImage(
                        model = profileUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.candidates),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Primary
                        )
                        Text("Add Profile", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 1. Symbol & Photo Section (At the top) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Symbol Photo Upload Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceLight)
                        .border(1.dp, if (symbolUri != null) Primary else OutlineColor, RoundedCornerShape(16.dp))
                        .clickable { 
                            symbolPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (symbolUri != null) {
                        AsyncImage(
                            model = symbolUri,
                            contentDescription = "Selected Symbol",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.candidates),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Primary
                            )
                            Text("Add Symbol", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = symbolName,
                        onValueChange = { symbolName = it },
                        label = { Text("Election Symbol Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("e.g., Rising Sun, Lion, Torch", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 2. Personal Basics ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name (Display Name)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Department / Course") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = college,
                onValueChange = { college = it },
                label = { Text("College Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))

            // --- 3. Position Dropdown ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = position,
                    onValueChange = { },
                    label = { Text("Position Applying For") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineColor
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(SurfaceLight)
                ) {
                    positions.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                position = selection
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))

            // --- 4. Campaign Content ---
            Text("Your Vision", style = MaterialTheme.typography.labelLarge, color = Primary, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = manifesto,
                onValueChange = { manifesto = it },
                label = { Text("Manifesto (Your Story & Vision)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = goals,
                onValueChange = { goals = it },
                label = { Text("Campaign Goals (Bullet points)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pledges,
                onValueChange = { pledges = it },
                label = { Text("My Pledge to Students") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(40.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isNotBlank() && position.isNotBlank()) {
                        viewModel.submitCandidateApplication(
                            userId = userId.toString(),
                            name = name,
                            studentId = "STU${userId}",
                            position = position,
                            manifesto = manifesto,
                            course = course,
                            college = college,
                            goals = goals,
                            pledges = pledges,
                            symbolName = symbolName,
                            photo = photoPath ?: "default_candidate.jpg",
                            symbol = symbolPath ?: "default_symbol.jpg"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SUBMIT CAMPAIGN PROFILE", fontWeight = FontWeight.Bold)
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

// Helper extension for Icon size
private fun Modifier.size(size: Int): Modifier = this.size(size.dp)
