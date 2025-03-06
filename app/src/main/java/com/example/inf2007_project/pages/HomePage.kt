package com.example.inf2007_project.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.health.connect.client.time.TimeRangeFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.R
import com.example.inf2007_project.uam.AuthState
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.testData
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontWeight
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import retrofit2.http.Query
import java.time.Instant
import kotlin.math.ceil


data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)
data class Consultation(
    val title: String,
    val type: String,
    val date: String,
    val time: String
)


@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val consultations = remember { mutableStateListOf<Triple<String, String, String>>() }
    val healthConnectClient = HealthConnectClient.getOrCreate(context)
    val heartRateData = remember { mutableStateOf("0") } // To store fetched heart rate data
    val stepsData = remember { mutableStateOf("0") } // Store fetched steps data
    val caloriesData = remember { mutableStateOf("0") } // Store fetched calories data
    val distData = remember { mutableStateOf("0") } // Store fetched distance
    var refreshTrigger by remember { mutableIntStateOf(0) } // Refresh trigger state

    LaunchedEffect(refreshTrigger) {
        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUser != null) {
            // Fetch documents where user_id matches the current user ID
            val consultationsResult = firestore.collection("consultations")
                .whereEqualTo("user_id", currentUser) // Filter by user_id
                .get()
                .await()

            // Clear and populate the documents list
            consultations.clear()
            consultations.addAll(consultationsResult.documents.mapNotNull { consultations ->
                val title = consultations.getString("title")
                val lastUpdated = consultations.getString("lastUpdated")
                val id = consultations.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })

            Log.d("FirestoreDebug", "Documents retrieved: $consultations")
        } else {
            Log.e("FirestoreDebug", "No user is currently logged in!")
        }

    }

    LaunchedEffect(Unit) {
        // Continuously fetch heart rate data every 5 seconds
        while (true) {
            val startTime = Instant.now().minusSeconds(6400) // One hour ago
            val endTime = Instant.now() // Current time
            readheartRateByTimeRange(healthConnectClient, startTime, endTime) { heartRate ->
                heartRateData.value = heartRate
            }

            readStepsByTimeRange(healthConnectClient, startTime, endTime) { steps ->
                stepsData.value = "$steps Steps"
            }

            readCaloriesByTimeRange(healthConnectClient, startTime, endTime) { cals ->
                 caloriesData.value = cals
            }

            readDistByTimeRange(healthConnectClient, startTime, endTime) { dist ->
                distData.value = dist
            }

            delay(5000) // Wait 5 seconds before fetching again
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
            Toast.makeText(context, "You have successfully signed out!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {

            // Greeting and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.sit_punggol),
                        contentDescription = "Profile Image",
                        modifier = Modifier.size(48.dp).padding(end = 8.dp)
                    )
                    Text("Hello!\nTan Kah Kee", fontSize = 24.sp)
                }
                Row {
                    IconButton(onClick = { /* Search Action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Assistant Action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Assistant")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vitals
            VitalsGrid(heartRate = heartRateData.value, steps = stepsData.value, calories = caloriesData.value, dist = distData.value)

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Consultations
            UpcomingConsultationsSection(navController, consultations)
        }
    }
}


@Composable
fun VitalsGrid(heartRate: String, steps: String, calories: String, dist: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            VitalCard(label = "Heart Rate", value = heartRate, unit = "Bpm")
            VitalCard(label = "Steps", value = steps, unit = "Steps")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VitalCard(label = "Calories", value = calories, unit = "kcal")
            VitalCard(label = "Distance", value = dist, unit = "m")
        }
    }
}


@Composable
fun VitalCard(label: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(width = 160.dp, height = 100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 18.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun UpcomingConsultationsSection(navController: NavController, consultations: List<Triple<String, String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Upcoming Consultations", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = { navController.navigate("consultations2") }) {
            Text("View All")
        }
    }

    if (consultations.isEmpty()) {
        Text("No upcoming consultations", fontSize = 14.sp, color = Color.Gray)
    }

    consultations.forEach { consultation ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(consultation.second, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${consultation.first}, ${consultation.third}", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


private suspend fun getConsultations(userId: String, onResult: (List<Consultation>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    try {
        val querySnapshot = db.collection("consultations")
            .whereEqualTo("userId", userId) // Filter by userId
            .get()
            .await()

        val consultations = querySnapshot.documents.mapNotNull { document ->
            document.toObject(Consultation::class.java)
        }

        onResult(consultations)
    } catch (e: Exception) {
        // Handle error (e.g., show a snackbar or log the error)
        onResult(emptyList())
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Clinics", Icons.Filled.ThumbUp, "clinics"),
        BottomNavItem("Notes", Icons.Filled.ThumbUp, "notes&reminders"),
        BottomNavItem("Dependency", Icons.Filled.Call, "dependencies"),

        BottomNavItem("Consultations", Icons.Filled.ThumbUp, "consultations2"),

        BottomNavItem("Profile", Icons.Filled.Person, "profile")
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label)
                },
                selected = false, // Implement logic for tracking selected state
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

suspend fun readheartRateByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant,
    onHeartRateFetched: (String) -> Unit
) {
    try {
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        // Loop through the heart rate records and log the values
        for (heartRateRecord in response.records) {
            val heartRate = heartRateRecord.samples.firstOrNull()?.beatsPerMinute ?: "0"
//            Log.d("HealthConnect", "Heart Rate: $heartRate")
            onHeartRateFetched("$heartRate bpm") // Pass the formatted data to callback
        }
    } catch (e: Exception) {
        Log.e("HealthConnect", "Error fetching heart rate: ${e.message}")
    }
}

suspend fun readStepsByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant,
    onStepsFetched: (String) -> Unit
) {
    try {
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        // Loop through the heart rate records and log the values
        for (steps in response.records) {
            val totalSteps = response.records.sumOf { it.count }
            Log.d("HealthConnectSteps", "Steps: $steps")
            onStepsFetched(totalSteps.toString()) // Pass the formatted data to callback
        }
    } catch (e: Exception) {
        Log.e("HealthConnectSteps", "Error fetching steps: ${e.message}")
    }

    suspend fun readStepsByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant,
        onStepsFetched: (String) -> Unit
    ) {
        try {
            val response =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )

            // Loop through the heart rate records and log the values
            for (steps in response.records) {
                Log.d("HealthConnectSteps", "Steps: $steps")
                onStepsFetched("$steps") // Pass the formatted data to callback
            }
        } catch (e: Exception) {
            Log.e("HealthConnectSteps", "Error fetching steps: ${e.message}")
        }
    }
}

suspend fun readCaloriesByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant,
    onCalsFetched: (String) -> Unit
) {
    try {
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        // Loop through the heart rate records and log the values
        if (response.records.isNotEmpty()) {
            val latestRecord = response.records.last() // Get the most recent record
            val latestCalories = latestRecord.energy.inKilocalories

            Log.d("HealthConnectCals", "Latest Calories Burned: $latestCalories kcal")

            // Pass the value as a formatted string
            onCalsFetched("%.2f kcal".format(latestCalories))
        } else {
            Log.d("HealthConnectCals", "No calorie data found.")
            onCalsFetched("0")
        }
    } catch (e: Exception) {
        Log.e("HealthConnectSteps", "Error fetching steps: ${e.message}")
    }
}

suspend fun readDistByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant,
    onDistFetched: (String) -> Unit
) {
    try {
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        // Loop through the heart rate records and log the values
        val totalDist = response.records.sumOf {
            // Remove the " meters" text from the string and parse the numeric part
            it.distance.toString().replace(" meters", "").trim().toDoubleOrNull() ?: 0.0
        }

        val roundedDist = String.format("%.2f", totalDist)

        Log.d("HealthConnectDist", "Total Distance: $roundedDist meters")
        onDistFetched(roundedDist)
    } catch (e: Exception) {
        Log.e("HealthConnectDist", "Error fetching dist: ${e.message}")
    }
}


