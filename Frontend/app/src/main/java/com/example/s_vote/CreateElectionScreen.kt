package com.example.s_vote

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.s_vote.navigation.Routes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.example.s_vote.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateElectionScreen(navController: NavController) {

    val roles = listOf("President", "Vice President", "Sports Secretary", "Cultural Secretary", "Discipline Secretary", "Treasurer")
    var expanded by remember { mutableStateOf(false) }
    var electionName by remember { mutableStateOf(roles[0]) }
    
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalTime.now().plusMinutes(5)) }
    
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusHours(1).plusMinutes(5)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val viewModel: com.example.s_vote.viewmodel.AdminViewModel = viewModel()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
            if(it.contains("created", ignoreCase = true)) {
                 navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Election") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                    )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("ELECTION CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = electionName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("SELECT ROLE") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Primary.copy(alpha = 0.1f),
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceLight)
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role, fontWeight = FontWeight.Medium) },
                            onClick = {
                                electionName = role
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Start Date & Time
            Text("STARTING PERIOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Text(startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), color = TextPrimary)
                }
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Text(startTime.format(DateTimeFormatter.ofPattern("HH:mm")), color = TextPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // End Date & Time
            Text("ENDING PERIOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Text(endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), color = TextPrimary)
                }
                OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Text(endTime.format(DateTimeFormatter.ofPattern("HH:mm")), color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Candidates are managed via Admin Home now, so this field is maybe just info or disabled
            Text("Add candidates via Manage Candidates Screen", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        val startCombined = "${startDate} ${startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                        val endCombined = "${endDate} ${endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                        viewModel.createElection(electionName, startCombined, endCombined)
                    }, 
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(Primary),
                    enabled = !isLoading
                ) {
                    Text("CREATE NOW", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                
                Button(
                    onClick = { navController.popBackStack() }, 
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.1f))
                ) {
                    Text("CANCEL", color = Primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
        
        // --- DIALOGS ---
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        if (showStartTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = startTime.hour, 
                initialMinute = startTime.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showStartTimePicker = false
                    }) { Text("OK") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }

        if (showEndTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = endTime.hour, 
                initialMinute = endTime.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showEndTimePicker = false
                    }) { Text("OK") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }
}
