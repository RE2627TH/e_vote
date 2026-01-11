package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.AppUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCandidateProfileScreen(navController: NavController, candidateId: String) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: com.example.s_vote.viewmodel.CandidateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val profile by viewModel.profile.collectAsState()

    // Fetch profile on load
    LaunchedEffect(candidateId) {
        viewModel.fetchProfile(candidateId)
    }

    // State holders
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var pledges by remember { mutableStateOf("") }

    // Image paths (server relative or full)
    var photoPath by remember { mutableStateOf<String?>(null) }
    var symbolPath by remember { mutableStateOf<String?>(null) }

    // Update state when profile loads
    LaunchedEffect(profile) {
        profile?.let {

                user: AppUser ->
            name = user.name
            user.candidateDetails?.let { details: Candidate ->
                course = details.course ?: ""
                college = details.college ?: ""
                tagline = details.tagline ?: ""
                goals = details.goals ?: ""
                pledges = details.pledges ?: ""
                photoPath = details.photo
                symbolPath = details.symbolUrl
            }
        }
    }

    // Image Pickers
    val contentResolver = context.contentResolver

    fun createMultipartBody(uri: android.net.Uri, paramName: String): okhttp3.MultipartBody.Part? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                // Use extension function for RequestBody (byte array) and MediaType
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                okhttp3.MultipartBody.Part.createFormData(paramName, "image_${System.currentTimeMillis()}.jpg", requestFile)
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val part = createMultipartBody(it, "image")
            if (part != null) {
                viewModel.uploadImageFile(part) { serverPath ->
                    if (serverPath != null) photoPath = serverPath
                }
            }
        }
    }

    val symbolLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val part = createMultipartBody(it, "image")
            if (part != null) {
                viewModel.uploadImageFile(part) { serverPath ->
                    if (serverPath != null) symbolPath = serverPath
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Your Profile") },
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Photo Upload Section
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Button(onClick = { photoLauncher.launch("image/*") }) {
                    Text("Upload Photo")
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (photoPath != null) {
                    val url = if (photoPath!!.startsWith("http")) photoPath else "${com.example.s_vote.api.ApiClient.BASE_URL}$photoPath"
                    coil.compose.AsyncImage(
                        model = url,
                        contentDescription = "Photo Preview",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .border(1.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Symbol Upload Section
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Button(onClick = { symbolLauncher.launch("image/*") }) {
                    Text("Upload Symbol")
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (symbolPath != null) {
                     val url = if (symbolPath!!.startsWith("http")) symbolPath else "${com.example.s_vote.api.ApiClient.BASE_URL}$symbolPath"
                     coil.compose.AsyncImage(
                        model = url,
                        contentDescription = "Symbol Preview",
                        modifier = Modifier.size(60.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course (e.g. B.Tech IT)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = college, onValueChange = { college = it }, label = { Text("College Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Campaign Tagline") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = goals, onValueChange = { goals = it }, label = { Text("Campaign Goals") }, modifier = Modifier.fillMaxWidth().height(120.dp), singleLine = false)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = pledges, onValueChange = { pledges = it }, label = { Text("My Pledges") }, modifier = Modifier.fillMaxWidth().height(120.dp), singleLine = false)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(candidateId, name, course, college, tagline, goals, pledges, photoPath, symbolPath)
                    // Optionally show toast or navigate back
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Changes")
            }
        }
    }
}