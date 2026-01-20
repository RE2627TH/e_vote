package com.example.s_vote

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.OcrState
import com.example.s_vote.viewmodel.OcrViewModel
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun ScanIdScreen(navController: NavController, candidateId: String, position: String) {
    val context = LocalContext.current
    val viewModel: OcrViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Permission State
    val cameraPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Initialize User Data Check
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("s_vote_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        val studentId = sharedPref.getString("STUDENT_ID", "") ?: ""
        
        if (userId.isEmpty() || (studentId.isEmpty() && sharedPref.getString("USER_ROLE", "").equals("student", true))) {
            Toast.makeText(context, "Session invalid. Please login again.", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    // UCrop Launcher
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                 val bitmap = if (Build.VERSION.SDK_INT < 28) {
                     MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                 } else {
                     val source = ImageDecoder.createSource(context.contentResolver, uri)
                     ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
                 }
                 viewModel.processIdCard(bitmap, context)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(result.data!!)
            Toast.makeText(context, "Crop Error: ${error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to launch crop
    fun launchCrop(sourceUri: Uri) {
        val destFile = File(context.cacheDir, "cropped_id_${System.currentTimeMillis()}.jpg")
        val destUri = Uri.fromFile(destFile)
        
        val options = UCrop.Options().apply {
             setCompressionFormat(Bitmap.CompressFormat.JPEG)
             setCompressionQuality(90)
             setFreeStyleCropEnabled(true)
             setToolbarTitle("Adjust ID Card")
             setToolbarColor(Color.Black.toArgb())
             setStatusBarColor(Color.Black.toArgb())
             setToolbarWidgetColor(Color.White.toArgb())
             setActiveControlsWidgetColor(Color(0xFF6743FF).toArgb())
        }

        val intent = UCrop.of(sourceUri, destUri)
            .withOptions(options)
            .getIntent(context)
        
        uCropLauncher.launch(intent)
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionState.value = isGranted
    }
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { launchCrop(it) }
    }

    // Handle State Changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is OcrState.Detected -> {
                val state = uiState as OcrState.Detected
                val encName = java.net.URLEncoder.encode(state.name, "UTF-8")
                val encDept = java.net.URLEncoder.encode(state.dept, "UTF-8")
                val encId = java.net.URLEncoder.encode(state.id, "UTF-8")
                val encPos = java.net.URLEncoder.encode(position, "UTF-8")
                navController.navigate("id_verified/$candidateId?name=$encName&dept=$encDept&id=$encId&position=$encPos") {
                    popUpTo(Routes.SCAN_ID) { inclusive = true }
                }
            }
            is OcrState.Verified -> {
                val data = (uiState as OcrState.Verified).studentData
                val encName = java.net.URLEncoder.encode(data.name, "UTF-8")
                val encDept = java.net.URLEncoder.encode(data.department, "UTF-8")
                val encId = java.net.URLEncoder.encode(data.studentId, "UTF-8")
                val encPos = java.net.URLEncoder.encode(position, "UTF-8")
                navController.navigate("id_verified/$candidateId?name=$encName&dept=$encDept&id=$encId&position=$encPos") {
                    popUpTo(Routes.SCAN_ID) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (cameraPermissionState.value) {
                CameraCaptureScreen(
                    onImageCaptured = { uri -> launchCrop(uri) },
                    onError = { 
                        Toast.makeText(context, "Camera Error: ${it.message}", Toast.LENGTH_SHORT).show() 
                    },
                    onClose = { navController.popBackStack() },
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onSkip = {
                        val encPos = java.net.URLEncoder.encode(position, "UTF-8")
                        navController.navigate("id_verified/$candidateId?name=&dept=&id=&position=$encPos") {
                            popUpTo(Routes.SCAN_ID) { inclusive = true }
                        }
                    }
                )
            } else {
                PermissionRequestScreen(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onCancel = { navController.popBackStack() }
                )
            }

            // Global Loading Overlay
            if (uiState is OcrState.Scanning || uiState is OcrState.Verifying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF6743FF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Analyzing ID Card...", 
                            color = Color.White, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Persistence Error UI
            if (uiState is OcrState.Error) {
                val errorMsg = (uiState as OcrState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = null, 
                            tint = Color.Red, 
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Scan Failed",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            errorMsg,
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.resetState() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Try Again", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit,
    onClose: () -> Unit,
    onGalleryClick: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // UI Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Bottom Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Capture Button at Center
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(8.dp)
                        .background(Color.White, CircleShape)
                        .clickable {
                            val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                            
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        val savedUri = Uri.fromFile(photoFile)
                                        onImageCaptured(savedUri)
                                    }
                                    override fun onError(exc: ImageCaptureException) {
                                        onError(exc)
                                    }
                                }
                            )
                        }
                )

                // Gallery on the Left
                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary, 
                        contentDescription = "Gallery", 
                        tint = Color.White, 
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0533)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Camera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
        Text(
            "Camera Access Needed",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF))
        ) {
            Text("Grant Permission")
        }
        TextButton(onClick = onCancel) {
            Text("Cancel", color = Color.Gray)
        }
    }
}
