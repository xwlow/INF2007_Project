package com.example.inf2007_project.consultation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.R
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.clinic.Booking
import com.example.inf2007_project.message.Message
import com.example.inf2007_project.message.MessageItem
import com.example.inf2007_project.notes.CardDialog
import com.example.inf2007_project.pages.BottomNavigationBar
import com.example.inf2007_project.uam.AuthViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ConsultationsPage2(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel) {

    // Get the current user ID
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    var consultations by remember { mutableStateOf(listOf<Booking>()) }
    var pastConsultations by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var upcomingConsultations by remember { mutableStateOf<List<Booking>>(emptyList()) }

    var selectedTabIndex by remember { mutableStateOf(1) }
    val tabs = listOf("Past", "Upcoming")


    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val query1 =  FirebaseFirestore.getInstance().collection("consultations")
                .whereEqualTo("userId", currentUser)

            query1.addSnapshotListener { snapshot1, e1 ->

                if (e1 != null) {
                    Log.e("FirestoreError", "Query failed: ${e1.message}")
                    return@addSnapshotListener
                }

                if (snapshot1 == null || snapshot1.isEmpty) {
                    Log.w("FirestoreWarning", "Query executed but no matching documents found.")
                    return@addSnapshotListener
                }

                consultations = snapshot1.documents.mapNotNull { doc ->
                    Booking(
                        userId = doc.getString("userId") ?: "",
                        selectedDate = doc.getString("selectedDate") ?: "",
                        doctorName = doc.getString("doctorName") ?: "",
                        chosenTime = doc.getString("chosenTime") ?: "",
                        clinicName = doc.getString("clinicName") ?: "",
                        extraInformation = doc.getString("extraInformation") ?: ""
                    )
                }

                // Log the results
                Log.d("FirestoreSuccess", "Query returned ${consultations.size} results.")
                consultations.forEach {
                    Log.d("MessageLog", "Booking: $it")
                }

                // Separate into past and upcoming based on date and time
                val now = Calendar.getInstance().time

                val pastList = mutableListOf<Booking>()
                val upcomingList = mutableListOf<Booking>()

                consultations.forEach { consultation ->
                    val consultationDateTime = convertToDateTime(consultation.selectedDate, consultation.chosenTime)
                    if (consultationDateTime != null) {
                        if (consultationDateTime.before(now)) {
                            pastList.add(consultation)
                        } else {
                            upcomingList.add(consultation)
                        }
                    }
                }

                // Update state
                pastConsultations = pastList
                upcomingConsultations = upcomingList
            }
        }
    }

    Log.d("Consultations", "Past $pastConsultations")
    Log.d("Consultations", "Upcoming $upcomingConsultations")

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
                    0 -> items(pastConsultations.size) { index ->
                        ConsultationItem(
                            consultation = pastConsultations[index]
                        )
                    }
                    1 -> items(upcomingConsultations.size) { index ->
                        ConsultationItem(
                            consultation = upcomingConsultations[index]
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConsultationItem(consultation: Booking) {

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
    }
}

// Convert selectedDate (dd/MM/yyyy) and chosenTime (hh:mm a) to a Date object
private fun convertToDateTime(date: String, time: String): Date? {
    return try {
        val dateTimeString = "$date $time"
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        formatter.parse(dateTimeString)
    } catch (e: Exception) {
        Log.e("DateConversion", "Error parsing date: $date $time", e)
        null
    }
}
