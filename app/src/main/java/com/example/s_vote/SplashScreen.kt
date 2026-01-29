package com.example.s_vote

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.s_vote.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.s_vote.navigation.Routes

@Composable
fun SplashScreen(navController: NavController) {
    // animation states
    var startAnim by remember { mutableStateOf(false) }

    // Enhanced entrance animations
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // start the animation and then wait -> navigate
    LaunchedEffect(Unit) {
        startAnim = true
        delay(2500)
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    // Modern Vibrant Gradient (Indigo -> Royal Blue -> Violet)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0533), // Deep Navy
            Primary,
            Secondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        // Subtle background blur/glow effects (Circles)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(y = (-50).dp, x = (-50).dp)
                .background(androidx.compose.ui.graphics.Color(0xFFFF3DA6).copy(alpha = 0.15f), CircleShape)
                .align(Alignment.TopStart)
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(y = 100.dp, x = 100.dp)
                .background(androidx.compose.ui.graphics.Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape)
                .align(Alignment.BottomEnd)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Pushed up slightly
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphism Logo Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(32.dp))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f), androidx.compose.ui.graphics.Color.Transparent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow shadow effect
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Secondary.copy(alpha = 0.3f), CircleShape)
                )
                
                Image(
                    painter = painterResource(id = R.drawable.ic_thumb_up),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
                CircularProgressIndicator(color = Secondary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Title with Letter Spacing
            Text(
                text = "E-VOTE",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pill Subtitle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .alpha(alpha)
            ) {
                Text(
                    text = "Secure • Verified • Fast",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }
        
        // Bottom Loading/Version Indicator
        Text(
            text = "PREMIUM EDITION",
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
