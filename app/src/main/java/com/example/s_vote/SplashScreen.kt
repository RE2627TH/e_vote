package com.example.s_vote

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

    // scale animation (0.6 -> 1.0)
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
    )

    // fade animation
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 700)
    )

    // start the animation and then wait -> navigate
    LaunchedEffect(Unit) {
        // small delay then animate in
        startAnim = true
        // show splash for 2000 ms total (adjust as needed)
        delay(2000)
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    // background: vertical gradient (deep indigo -> purple -> magenta)
    val gradientBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF30216E),
            androidx.compose.ui.graphics.Color(0xFF6A4CFF),
            androidx.compose.ui.graphics.Color(0xFFFF3DA6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        // main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // badge: rounded square with thumb icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(22.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF3E1F7F)),
                contentAlignment = Alignment.Center
            ) {
                // icon (vector drawable)
                Image(
                    painter = painterResource(id = R.drawable.ic_thumb_up),
                    contentDescription = "thumb",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // pill title
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF29123F).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text("E-Vote", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Secure & verified Election",
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
