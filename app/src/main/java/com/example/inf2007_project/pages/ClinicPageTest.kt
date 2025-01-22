package com.example.inf2007_project.pages

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.AuthViewModel
import com.example.inf2007_project.ClinicViewModel
import com.example.inf2007_project.NearbySearchViewModel
import com.example.inf2007_project.TestViewModel

@Composable
fun ClinicsPageTest(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel, nearbySearchViewModel: NearbySearchViewModel) {
//    var location by remember { mutableStateOf("1.3521,103.8198") } // Example: Singapore's coordinates
//    var radius by remember { mutableStateOf(1500) }
//    var type by remember { mutableStateOf("restaurant") }
//    val apiKey = "YOUR_API_KEY"

    // Sample test inputs
    val keyword = "clinic"
    val location = "1.4039679,103.7373203"
    val radius = 1000
    val apiKey = "AIzaSyDZ7GHGvGgfAcldVbm8-zrM4Fghsds89CA"
    // Fetch clinics data when the composable is launched
    LaunchedEffect(Unit) {
        nearbySearchViewModel.fetchNearbyPlaces(keyword, location, radius, apiKey)
    }

    // Observe the clinics data
    val places = nearbySearchViewModel.places

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Padding to avoid overlapping with the bottom bar
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Clinics Test Page", fontSize = 32.sp)

            // Display the fetched clinic data in a LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display a loading message if clinics are empty
                if (places.isEmpty()) {
                    item {
                        Text("Loading clinics data...")
                    }
                }
                else {
                    // Use the 'items' function to iterate over places
                    items(places) { place ->
                        Button(
                            onClick = {
                                val encodedClinicInfo = Uri.encode("${place.name}|${place.vicinity}")
                                navController.navigate("queue/$encodedClinicInfo")
                            },
                        ) {
                            Text(text = place.name + " " +place.vicinity)
                        }
                    }
                }
            }
        }
    }
}