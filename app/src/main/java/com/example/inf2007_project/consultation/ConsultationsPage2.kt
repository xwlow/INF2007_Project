package com.example.inf2007_project.consultation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.R
import com.example.inf2007_project.clinic.BookViewModel
import com.example.inf2007_project.clinic.Booking
import com.example.inf2007_project.pages.BottomNavigationBar

@Composable
fun ConsultationsPage2(modifier: Modifier = Modifier, navController : NavController, bookViewModel: BookViewModel) {

    // Collect state from ViewModel
    val pastConsultations by bookViewModel.pastConsultations.collectAsState()
    val upcomingConsultations by bookViewModel.upcomingConsultations.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(1) }
    val tabs = listOf("Past", "Upcoming")

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text (
                text = "Consultations",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            LazyColumn {
                when (selectedTabIndex) {
                    0 -> {
                        if (pastConsultations.isEmpty()) {
                            item {
                                Text(
                                    text = "No past consultations found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                        } else {
                            items(pastConsultations.size) { index ->
                                ConsultationItem(
                                    consultation = pastConsultations[index],
                                    bookViewModel
                                )
                            }
                        }
                    }
                    1 -> {
                        if (upcomingConsultations.isEmpty()) {
                            item {
                                Text(
                                    text = "No upcoming consultations.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Add spacing between buttons
                                // Book an consultation Btn
                                Button(
                                    onClick = {
                                        navController.navigate("clinicsTest")
                                    }, modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Book a Consultation")
                                }
                            }
                        } else {
                            items(upcomingConsultations.size) { index ->
                                ConsultationItem(
                                    consultation = upcomingConsultations[index],
                                    bookViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsultationItem(consultation: Booking, bookViewModel: BookViewModel) {
    // Modal
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // Sample image
            Image(
                painter = painterResource(id = R.drawable.sit_punggol),
                contentDescription = "Clinic Image",
                modifier = Modifier
                    .size(
                        width = 100.dp,
                        height = 150.dp
                    ), // Set custom width and height
                contentScale = ContentScale.Crop // Ensure the image is cropped to fit the specified size
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(consultation.clinicName)
                Text(consultation.selectedDate + " @ " + consultation.chosenTime)
                Row {
                    // Cancel Btn
                    Button(
                        onClick = {
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing between buttons

                    // Reschedule Btn
                    Button(
                        onClick = {

                        },
                    ) {
                        Text(text = "Reschedule")
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
                    Text(text = "Cancel Consultation?")
                },
                text = {
                    Text(text = "Your appointment is currently scheduled at ${consultation.clinicName}, ${consultation.selectedDate} @ ${consultation.chosenTime} for a checkup")
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            bookViewModel.deleteBooking(
                                consultationId = consultation.consultationId,
                                onSuccess = { Log.d("Firestore", "Deleted successfully!") },
                                onFailure = { e -> Log.e("Firestore", "Failed to delete", e) }
                            )
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
}
