package com.example.inf2007_project.clinic

import android.net.Uri
import android.util.Log
import android.widget.CalendarView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPage(
    clinicName: String,
    clinicStreetName: String,
    clinicID: String,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    // Calender
    var selectedDate by remember { mutableStateOf("") }
    // Name
    var name by remember { mutableStateOf("") }
    // Doctors
    var doctorsExpanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    var doctorName by remember { mutableStateOf("") }
    val doctorIcon = if (doctorsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    // List of doctor names placeholder
    val doctorList = listOf(
        "Dr. John Smith",
        "Dr. Emily Wong",
        "Dr. Michael Lee",
        "Dr. Sarah Patel",
        "Dr. David Kim"
    )
    // List of consultation slot timings (8:00 AM to 5:00 PM, every 15 mins)
    var chosenTime by remember { mutableStateOf("") }
    var timeExpanded by remember { mutableStateOf(false) }
    val timeIcon = if (timeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val consultationSlots = generateConsultationSlots(
        startHour = 8, // Clinic opens at 8 AM
        endHour = 17,  // Clinic closes at 5 PM
        intervalMinutes = 15 // Each consultation lasts 15 minutes
    )
    // Extra
    var extraInformation by remember { mutableStateOf("") }
    // Modal
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = clinicName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {

                    // Calendar
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Selected Date: ${selectedDate}")
                        AndroidView(
                            factory = { context ->
                                CalendarView(context).apply {
                                    // Set initial date to today
                                    val today = System.currentTimeMillis()
                                    // One day
                                    val oneDayInMillis = 24 * 60 * 60 * 1000
                                    date = today
                                    minDate = today + oneDayInMillis

                                    // Listen for date change
                                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                                        // Format selected date
                                        val formattedDate = "$dayOfMonth/${month + 1}/$year"
                                        selectedDate = formattedDate
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name, onValueChange = {
                        name = it
                    },
                    label = {
                        Text(text = "Enter Your Name")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                // Dropdown for Doctors
                Box {
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { doctorsExpanded = true }
                            .onGloballyPositioned { coordinates ->
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        label = { Text("Request for a specific doctor") },
                        trailingIcon = {
                            Icon(
                                doctorIcon,
                                "Dropdown",
                                Modifier.clickable { doctorsExpanded = !doctorsExpanded })
                        }
                    )

                    DropdownMenu(
                        expanded = doctorsExpanded,
                        onDismissRequest = { doctorsExpanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                    ) {
                        doctorList.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    doctorName = label
                                    doctorsExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown for Clinic Timings
                Box {
                    OutlinedTextField(
                        value = chosenTime,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timeExpanded = true }
                            .onGloballyPositioned { coordinates ->
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        label = { Text("Timing for Consultation") },
                        trailingIcon = {
                            Icon(
                                timeIcon,
                                "Dropdown",
                                Modifier.clickable { timeExpanded = !timeExpanded })
                        }
                    )

                    DropdownMenu(
                        expanded = timeExpanded,
                        onDismissRequest = { timeExpanded = false },
                        modifier = Modifier.size(300.dp).width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                    ) {
                        consultationSlots.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    chosenTime = label
                                    timeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Extra Information Input Box
                OutlinedTextField(
                    value = extraInformation,
                    onValueChange = {
                        extraInformation = it
                    },
                    label = {
                        Text(text = "Anything we have to know?")
                    },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Book Btn
                Button(
                    onClick = {
                        showDialog = true
                    },
                    enabled = if (name.isNotEmpty() and doctorName.isNotEmpty() and selectedDate.isNotEmpty() and chosenTime.isNotEmpty()) true else false
                ) {
                    Text(text = "Book")
                }
            }
        }
    }

    // Popup Modal with AlertDialog
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "Confirm Booking?")
            },
            text = {
                Text(text = "Your appointment will be at $clinicStreetName, $selectedDate @ $chosenTime for $doctorName")
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        // Confirm Booking Action
                        showDialog = false
                        val encodedClinicInfo = Uri.encode("${clinicName}|${clinicStreetName}|${clinicID}")
                        val encodedBookingInfo = Uri.encode("${selectedDate}|${chosenTime}")
                        navController.navigate("SuccessBooking/$encodedClinicInfo/$encodedBookingInfo")
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        // Close the dialog
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun generateConsultationSlots(startHour: Int, endHour: Int, intervalMinutes: Int): List<String> {
    val slots = mutableListOf<String>()
    var currentHour = startHour
    var currentMinute = 0

    while (currentHour < endHour || (currentHour == endHour && currentMinute == 0)) {
        // Format time to AM/PM format
        val formattedTime = String.format(
            "%02d:%02d %s",
            if (currentHour > 12) currentHour - 12 else currentHour, // Convert to 12-hour format
            currentMinute,
            if (currentHour >= 12) "PM" else "AM"
        )
        slots.add(formattedTime)

        // Increment time by the interval
        currentMinute += intervalMinutes
        if (currentMinute >= 60) {
            currentMinute = 0
            currentHour++
        }
    }

    return slots
}


