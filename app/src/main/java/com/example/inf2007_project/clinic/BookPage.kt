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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.inf2007_project.uam.DependencyData
import com.example.inf2007_project.uam.UserDetailData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPage(
    clinicName: String,
    modifier: Modifier = Modifier,
    navController: NavController,
    bookViewModel: BookViewModel,
    existingBooking: Booking? = null
) {
    // Updates
    val isUpdating = existingBooking != null
    // Calender
    var selectedDate by remember { mutableStateOf("") }
    var dependencyName by remember { mutableStateOf("") }
    var updatesFlag by remember { mutableStateOf(false) }
    // Dependency
    val firestore = FirebaseFirestore.getInstance()
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var dependencyExpended by remember { mutableStateOf(false) }
    val dependencyIcon = if (dependencyExpended) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    var dependencyIdSelected by remember { mutableStateOf("") }
    var selectedDependencyName by remember { mutableStateOf("") }
    //var dependencies by remember { mutableStateOf(emptyList<DependencyData>()) }
    var dependenciesWithDetails by remember { mutableStateOf(emptyList<Pair<DependencyData, UserDetailData>>()) }
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
    //val consultationSlots = bookViewModel.availableSlots
    val availableSlots by bookViewModel.availableSlots.collectAsState(initial = emptyList())
    // Extra
    var extraInformation by remember { mutableStateOf("") }
    // Modal
    var showDialog by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }

    // Ensure dependency data exists for user
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("dependencies")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("Firestore Error", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val fetchedDependencies = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DependencyData::class.java)?.let { dependency ->
                                val documentId = doc.id
                                val dependencyId = dependency.dependencyId ?: documentId

                                Log.d(
                                    "Firestore Data",
                                    "Retrieved Dependency: ${dependency.dependencyId}, Doc ID: $documentId, Dependency ID: $dependencyId"
                                )

                                dependency.copy(
                                    dependencyId = dependencyId,
                                    documentId = documentId
                                )
                            }
                        }

                        // Fetch user details for each dependencyId
                        fetchedDependencies.forEach { dependency ->
                            dependency.dependencyId?.let {
                                firestore.collection("userDetail")
                                    .document(it) // Use dependencyId to fetch user details
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        val userDetails = userDoc.toObject(UserDetailData::class.java)

                                        if (userDetails != null) {
                                            // Store both dependency and user details
                                            dependenciesWithDetails = dependenciesWithDetails + Pair(dependency, userDetails)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Firestore Error", "Failed to fetch user details", exception)
                                    }
                            }
                        }
                    }
                }
        }
    }


    // Ensure state updates when existingBooking changes
    LaunchedEffect(existingBooking) {
        existingBooking?.let {
            selectedDate = it.selectedDate
            doctorName = it.doctorName
            chosenTime = it.chosenTime
            extraInformation = it.extraInformation

            dependenciesWithDetails.find {it.first.dependencyId == existingBooking.dependencyId}?.let { (dependency, userDetails) ->
                selectedDependencyName = "${userDetails.name} (${dependency.relationship})"
            }


        }

        Log.d("Selected Dependency", selectedDependencyName)
        delay(300)
        updatesFlag = true
        showCalendar = true
    }

    bookViewModel.fetchAvailableTimings(clinicName, selectedDate)

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
                        Text(text = "Selected Date: $selectedDate")
                        if (showCalendar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        CalendarView(context).apply {
                                            val today = System.currentTimeMillis()
                                            val oneDayInMillis = 24 * 60 * 60 * 1000

                                            if (selectedDate.isNotEmpty()) {
                                                val parsedDate = parseDateToMillis(selectedDate)
                                                date = parsedDate
                                            } else {
                                                date = today
                                            }

                                            minDate = today + oneDayInMillis

                                            setBackgroundColor(android.graphics.Color.WHITE)
                                            setOnDateChangeListener { _, year, month, dayOfMonth ->
                                                val formattedDate = "$dayOfMonth/${month + 1}/$year"
                                                selectedDate = formattedDate
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Dropdown for Dependecy
                Box {
                    OutlinedTextField(
                        value = selectedDependencyName,
                        onValueChange = {},
                        enabled = dependenciesWithDetails.isNotEmpty(),
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dependencyExpended = true }
                            .onGloballyPositioned { coordinates ->
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        label = { Text("Specify the dependency") },
                        trailingIcon = {
                            Icon(
                                dependencyIcon,
                                "Dropdown",
                                Modifier.clickable(enabled = dependenciesWithDetails.isNotEmpty()) { dependencyExpended = !dependencyExpended })
                        }
                    )

                    DropdownMenu(
                        expanded = dependencyExpended,
                        onDismissRequest = { dependencyExpended = false },
                        modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                    ) {
                        // Change the list
                        dependenciesWithDetails.forEach { (dependency, userDetails) ->
                            DropdownMenuItem(
                                text = { Text(text = "${userDetails.name} (${dependency.relationship})") },
                                onClick = {
                                    dependencyIdSelected = dependency.dependencyId.toString()
                                    selectedDependencyName = "${userDetails.name} (${dependency.relationship})"
                                    dependencyExpended = false
                                }
                            )
                        }
                    }
                }

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
                        enabled = selectedDate.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedDate.isNotEmpty()) { timeExpanded = true }
                            .onGloballyPositioned { coordinates ->
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        label = { Text("Timing for Consultation") },
                        trailingIcon = {
                            Icon(
                                timeIcon,
                                "Dropdown",
                                Modifier.clickable(enabled = selectedDate.isNotEmpty()) { timeExpanded = !timeExpanded })
                        }
                    )

                    DropdownMenu(
                        expanded = timeExpanded,
                        onDismissRequest = { timeExpanded = false },
                        modifier = Modifier.size(300.dp).width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                    ) {
                        availableSlots.forEach { label ->
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
                    enabled = if (doctorName.isNotEmpty() and selectedDate.isNotEmpty() and chosenTime.isNotEmpty()) true else false
                ) {
                    Text(text = if (isUpdating) "Update Booking" else "Book")
                }
            }
        }
    }
    // Show Confirmation Dialog
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm ${if (isUpdating) "Update" else "Booking"}") },
            text = { Text("Are you sure you want to ${if (isUpdating) "update" else "book"} this consultation?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        val encodedBookingInfo = Uri.encode("${selectedDate}|${chosenTime}")
                        navController.navigate("SuccessBooking/$encodedBookingInfo")
                        if (isUpdating) {
                            bookViewModel.updateBooking(
                                bookingId = existingBooking!!.consultationId,
                                //selectedDependencyName = selectedDependencyName,
                                //dependencyName = dependencyName,
                                selectedDate = selectedDate,
                                doctorName = doctorName,
                                chosenTime = chosenTime,
                                clinicName = clinicName,
                                extraInformation = extraInformation
                            )
                        } else {
                            bookViewModel.saveBooking(
                                selectedDate = selectedDate,
                                doctorName = doctorName,
                                chosenTime = chosenTime,
                                clinicName = clinicName,
                                extraInformation = extraInformation,
                                //selectedDependencyName = selectedDependencyName,
                                dependencyId = dependencyIdSelected,
                                onSuccess = {
                                    Log.e("Firestore", "Booking saved")
                                },
                                onFailure = { exception ->
                                    Log.e("Firestore", "Failed to save booking", exception)
                                }
                            )
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

fun parseDateToMillis(dateStr: String): Long {
    return try {
        val trimmedDate = dateStr.trim()

        if (trimmedDate.isEmpty()) {
            Log.e("horra", "selectedDate is empty, using current date.")
            return System.currentTimeMillis()
        }

        Log.d("horra", "Trying to parse: $trimmedDate")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(trimmedDate)

        if (date != null) {
            Log.d("horra", "Parsed successfully: ${date.time}")
            date.time
        } else {
            Log.e("horra", "Parsing failed, using default date")
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        Log.e("horra", "Error parsing date: ${e.message}")
        System.currentTimeMillis() // Default to today if parsing fails
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


