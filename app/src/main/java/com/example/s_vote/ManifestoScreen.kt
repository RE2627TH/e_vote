package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestoScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manifesto Highlights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },

                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF18048A),
                            titleContentColor = Color.White
                    )
            )
        }
    ) { padding ->

        val manifestoList = listOf(
            "Digital voting system improvements",
            "More cultural & technical events",
            "Improved library & study spaces",
            "Better campus transportation",
            "Career guidance & coding bootcamps"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Key Promises",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A0033)
            )

            Spacer(modifier = Modifier.height(16.dp))

            manifestoList.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEDE7FF))
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "• $item",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3E1F7F)
                    )
                }
            }
        }
    }
}
