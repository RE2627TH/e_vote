package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.s_vote.model.AppNotification
import com.example.s_vote.navigation.Routes
import com.example.s_vote.ui.theme.*
import com.example.s_vote.api.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val userId = sessionManager.getUserId() ?: ""
    val scope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.api.getNotifications(userId)
                if (response.isSuccessful && response.body() != null) {
                    notifications = response.body()!!.notifications
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "NOTIFICATIONS", 
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
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitInstance.api.getNotifications(userId)
                                if (response.isSuccessful && response.body() != null) {
                                    notifications = response.body()!!.notifications
                                }
                            } catch (e: Exception) { }
                            finally { isLoading = false }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Primary)
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
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = TextMuted
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification) {
                        if (notification.screen == "MANAGE_CANDIDATES") {
                            navController.navigate(Routes.MANAGE_CANDIDATES)
                        } else if (notification.screen == "ADMIN_CANDIDATE_REVIEW" && notification.dataId != null) {
                            navController.navigate(Routes.adminCandidateReview(notification.dataId))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: AppNotification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead == 0) SurfaceLight else BackgroundLight
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (notification.isRead == 0) Primary.copy(alpha = 0.1f) else OutlineColor
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (notification.isRead == 0) Primary.copy(alpha = 0.1f) else TextMuted.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = null, 
                    tint = if (notification.isRead == 0) Primary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    notification.title.uppercase(), 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Black, 
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    notification.body, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    notification.createdAt, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
