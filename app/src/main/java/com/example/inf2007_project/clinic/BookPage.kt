package com.example.inf2007_project.clinic

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
import androidx.compose.foundation.layout.width
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
    var expanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    var doctorName by remember { mutableStateOf("") }
    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    // List of doctor names
    val doctorList = listOf(
        "Dr. John Smith",
        "Dr. Emily Wong",
        "Dr. Michael Lee",
        "Dr. Sarah Patel",
        "Dr. David Kim"
    )
    //Extra
    var extraInformation by remember { mutableStateOf("") }

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card (modifier = Modifier
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
                        .clickable { expanded = true }
                        .onGloballyPositioned { coordinates -> mTextFieldSize = coordinates.size.toSize() },
                    label = { Text("Request for a specific doctor") },
                    trailingIcon = { Icon(icon, "Dropdown", Modifier.clickable { expanded = !expanded }) }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                ) {
                    doctorList.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label) },
                            onClick = {
                                doctorName = label
                                expanded = false
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
            Button(onClick = {
                //navController.navigate("login_screen")

            }, enabled = if (name.isNotEmpty() and extraInformation.isNotEmpty() and doctorName.isNotEmpty() and selectedDate.isNotEmpty()) true else false
            ) {
                Text(text = "Book")
            }
        }
    }
}
