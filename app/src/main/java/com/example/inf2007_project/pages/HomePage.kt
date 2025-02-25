package com.example.inf2007_project.pages

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


data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

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
            VitalsGrid()

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Consultations
            UpcomingConsultationsSection(navController)
        }
    }
}


@Composable
fun VitalsGrid() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            VitalCard(label = "Heartrate", value = "78", unit = "Bpm")
            VitalCard(label = "Oxygen", value = "98", unit = "%")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VitalCard(label = "Temperature", value = "36", unit = "°C")
            VitalCard(label = "Blood Pressure", value = "131/76", unit = "")
        }
    }
}

@Composable
fun VitalCard(label: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.48f)
            .padding(4.dp),
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
                "$value $unit",
                fontSize = 24.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun UpcomingConsultationsSection(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Upcoming Consultations", fontSize = 18.sp)
        TextButton(onClick = { navController.navigate("consultations2") }) {
            Text("View All")
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SIT @ Punggol", fontSize = 16.sp)
            Text("Check Up, 17 October 2024 @ 10 AM", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        //BottomNavItem("Clinics", Icons.Filled.ThumbUp, "clinics"),
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
