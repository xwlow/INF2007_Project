package com.example.inf2007_project.clinic

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun ClinicsPageTest(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel, nearbySearchViewModel: NearbySearchViewModel) {
    val keyword = "clinic"
    val radius = 500 //change for the time being cus yew tee coffeebean got no nearby clinic
    val apiKey = "AIzaSyDZ7GHGvGgfAcldVbm8-zrM4Fghsds89CA"
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // State variables
    var location by remember { mutableStateOf("Unknown location") }
    var locationFetched by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Fetch current location
            startLocationUpdates(fusedLocationClient) { currentLocation ->
                location = currentLocation
                locationFetched = true
            }
        } else {
            location = "Permission Denied"
        }
    }

    // Fetch clinics data when the composable is launched
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Re-fetch clinics whenever the location is updated
    LaunchedEffect(location) {
        if (locationFetched && location != "Permission Denied" && location != "Unknown location") {
            // Log the updated location
            Log.d("Location", location)
            // Call the API to fetch nearby clinics
            nearbySearchViewModel.fetchNearbyPlaces(keyword, location, radius, apiKey)
        }
    }

    // Observe the clinics data
    val places = nearbySearchViewModel.places
    Log.d("Fetched Clinics", places.toString())

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
                } else {
                    // Use the 'items' function to iterate over places
                    items(places) { place ->
                        Button(
                            onClick = {
                                val encodedClinicInfo = Uri.encode("${place.name}|${place.vicinity}|${place.place_id}")
                                navController.navigate("clinic/$encodedClinicInfo")
                            },
                        ) {
                            Text(text = place.name + " " + place.vicinity)
                        }
                    }
                }
            }
        }
    }
}
//
//@SuppressLint("MissingPermission")
//fun getCurrentLocation(
//    fusedLocationClient: FusedLocationProviderClient,
//    onLocationReceived: (String) -> Unit
//) {
//    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//        if (location != null) {
//            val latitude = location.latitude
//            val longitude = location.longitude
//            onLocationReceived("$latitude,$longitude")
//            Log.d("Coordinates", onLocationReceived.toString())
//        } else {
//            onLocationReceived("Unable to retrieve location")
//            Log.d("Coordinates", "Unable to retrieve location")
//        }
//    }
//}

@SuppressLint("MissingPermission") // Ensure permissions are granted before calling this method
fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdated: (String) -> Unit
) {
    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
        //        interval = 5000 // Set the desired update interval (in milliseconds)
//        fastestInterval = 2000 // Set the fastest interval for location updates
//        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        Priority.PRIORITY_HIGH_ACCURACY, 20000).setMinUpdateDistanceMeters(100f).setMinUpdateIntervalMillis(2000).build()



    val locationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                onLocationUpdated("$latitude,$longitude")
                Log.d("DynamicLocation", "$latitude,$longitude")
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
}
