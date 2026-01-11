package com.example.s_vote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import com.example.s_vote.viewmodel.OcrState
import com.example.s_vote.viewmodel.OcrViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

    // Queue to open camera immediately if possible
    var shouldOpenCamera by remember { mutableStateOf(cameraPermissionState.value) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualIdText by remember { mutableStateOf("") }

    // Initialize User Data
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("s_vote_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        if (userId.isNotEmpty()) {
            // Valid session
        } else {
            Toast.makeText(context, "User session invalid. Please log in again.", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionState.value = isGranted
        if (isGranted) shouldOpenCamera = true
    }
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
            }
            viewModel.processIdCard(bitmap, context)
        }
    }

    // Handle State Changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is OcrState.Verified -> {
                val data = (uiState as OcrState.Verified).studentData
                val verifiedRoute = Routes.voteConfirmation(
                    candidateId = candidateId,
                    position = position,
                    name = data.name,
                    dept = data.department,
                    id = data.studentId
                )
                navController.navigate(verifiedRoute)
            }
            is OcrState.Error -> {
                Toast.makeText(context, (uiState as OcrState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Black // Dark background for premium feel
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            
            if (shouldOpenCamera && cameraPermissionState.value) {
                // LIVE CAMERA VIEW
                CameraView(
                    onImageCaptured = { bitmap ->
                        viewModel.processIdCard(bitmap, context)
                    },
                    onError = { exc ->
                        Toast.makeText(context, "Camera Error: ${exc.message}", Toast.LENGTH_SHORT).show()
                    },
                    onClose = { navController.popBackStack() },
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onManualClick = { showManualDialog = true }
                )
            } else {
                // PERMISSION REQUEST / STARTUP SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0F0533), Color(0xFF18048A))
                            )
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera",
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(24.dp),
                        tint = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Scan ID Card",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "We need camera access to scan your college ID card for verification.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF)),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Allow Camera Access", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload from Gallery", color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }

            // Global Loading Indicator
            if (uiState is OcrState.Scanning || uiState is OcrState.Verifying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF6743FF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Verifying ID...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            }

            if (showManualDialog) {
                AlertDialog(
                    onDismissRequest = { showManualDialog = false },
                    title = { Text("Enter Student ID") },
                    text = {
                        OutlinedTextField(
                            value = manualIdText,
                            onValueChange = { manualIdText = it },
                            label = { Text("Student ID / Register No") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showManualDialog = false
                                if (manualIdText.isNotEmpty()) {
                                    viewModel.verifyStudentId(manualIdText)
                                }
                            },
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6743FF))
                        ) {
                            Text("Verify")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

@Composable
fun CameraView(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (Exception) -> Unit,
    onClose: () -> Unit,
    onGalleryClick: () -> Unit,
    onManualClick: () -> Unit
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
                Log.e("CameraView", "Use case binding failed", exc)
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Scanner Overlay
        ScannerOverlay()
        
        // --- CONTROLS ---

        // Top Bar (Close Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Header Text
            Text(
                "Scan ID Card",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Placeholder for symmetry or flash toggle in future
            Spacer(modifier = Modifier.size(40.dp)) 
        }

        // Bottom Bar (Capture & Gallery)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align ID card within the frame",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onGalleryClick() }) {
                     Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(32.dp))
                     Text("Gallery", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                // Capture Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxy.toBitmap()
                                        val rotation = imageProxy.imageInfo.rotationDegrees
                                        val rotatedBitmap = if (rotation != 0) {
                                            val matrix = Matrix()
                                            matrix.postRotate(rotation.toFloat())
                                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                        } else {
                                            bitmap
                                        }
                                        onImageCaptured(rotatedBitmap)
                                        imageProxy.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        onError(exception)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ring
                    Box(modifier = Modifier.size(80.dp).border(4.dp, Color.White, CircleShape))
                    // Inner Fill
                    Box(modifier = Modifier.size(64.dp).background(Color(0xFF6743FF), CircleShape))
                }
                
                // Empty spacer for balance
                // Manual Entry Button (Right Side)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    modifier = Modifier.clickable { onManualClick() }
                ) {
                     Icon(Icons.Default.Edit, contentDescription = "Manual", tint = Color.White, modifier = Modifier.size(32.dp))
                     Text("Manual", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        
        // Full Screen Scanner (with small padding)
        val cardWidth = width * 0.95f
        // Let's make it cover 80% of height to allow controls at bottom
        val cardHeight = height * 0.75f 
        
        val left = (width - cardWidth) / 2
        // Center vertically in the available space (roughly)
        val top = (height - cardHeight) / 2 - 50.dp.toPx() // Shift up slightly to avoid hitting buttons
        
        val right = left + cardWidth
        val bottom = top + cardHeight
        val cornerRad = 12.dp.toPx()

        // 1. Draw Dimmed Background
        val dimColor = Color.Black.copy(alpha = 0.6f)
        // Top
        drawRect(dimColor, topLeft = Offset(0f, 0f), size = Size(width, top))
        // Bottom
        drawRect(dimColor, topLeft = Offset(0f, bottom), size = Size(width, height - bottom))
        // Left
        drawRect(dimColor, topLeft = Offset(0f, top), size = Size(left, cardHeight))
        // Right
        drawRect(dimColor, topLeft = Offset(right, top), size = Size(width - right, cardHeight))

        // 2. Draw Full Border (Rounded Rectangle)
        drawRoundRect(
            color = Color(0xFF6743FF),
            topLeft = Offset(left, top),
            size = Size(cardWidth, cardHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRad),
            style = Stroke(width = 4.dp.toPx())
        )
        
        // 3. Scanning Laser
        val scanY = top + (cardHeight * scanAnim)
        
        // Laser Line
        drawLine(
            color = Color(0xFFA890FF), 
            start = Offset(left + 10f, scanY),
            end = Offset(right - 10f, scanY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Laser Glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF6743FF).copy(alpha = 0f),
                    Color(0xFF6743FF).copy(alpha = 0.4f),
                    Color(0xFF6743FF).copy(alpha = 0f)
                )
            ),
            topLeft = Offset(left, scanY - 20.dp.toPx()),
            size = Size(cardWidth, 40.dp.toPx())
        )
    }
}

