package com.example.s_vote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.setValue
import com.example.s_vote.navigation.AppNavGraph
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.EvoteTheme
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private var targetScreen by mutableStateOf<String?>(null)
    private var targetDataId by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🛡️ Request Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 🔔 Handle Deep Link from Notification
        targetScreen = intent.getStringExtra("screen")
        targetDataId = intent.getStringExtra("data_id")

        setContent {
            val navController = rememberNavController()
            
            LaunchedEffect(targetScreen, targetDataId) {
                // 🔄 Sync FCM Token on Startup
                val sessionManager = SessionManager(this@MainActivity)
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            val scope = kotlinx.coroutines.MainScope()
                            scope.launch {
                                try {
                                    com.example.s_vote.api.RetrofitInstance.api.updateFcmToken(mapOf(
                                        "user_id" to userId,
                                        "fcm_token" to token
                                    ))
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "FCM Sync failed: ${e.message}")
                                }
                            }
                        }
                    }
                }

                targetScreen?.let { screen ->
                    if (screen == "MANAGE_CANDIDATES") {
                        navController.navigate(Routes.MANAGE_CANDIDATES)
                    } else if (screen == "ADMIN_CANDIDATE_REVIEW" && targetDataId != null) {
                        navController.navigate(Routes.adminCandidateReview(targetDataId!!))
                    }
                    // Reset after navigation
                    targetScreen = null
                    targetDataId = null
                }
            }

            EvoteTheme {
                AppNavGraph(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetScreen = intent.getStringExtra("screen")
        targetDataId = intent.getStringExtra("data_id")
    }
}
