package com.example.s_vote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQs & Voter Education") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B039A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        val faqList = listOf(
            "How does voting work?" to "Scan your ID → Select candidate → Submit.",
            "Is the vote anonymous?" to "Yes. Your identity is not linked to vote.",
            "Can I vote twice?" to "No. System blocks repeated voting.",
            "What is the voting time?" to "Displayed on home countdown timer."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)   // 👈 ADD THIS
                .padding(padding)
                .padding(16.dp)
        ) {


            faqList.forEach { (question, answer) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(question, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(answer)
                    }
                }
            }
        }
    }
}