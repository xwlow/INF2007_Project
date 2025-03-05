package com.example.inf2007_project.clinic

import Place
import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.R
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun ClinicsPageTest(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel, nearbySearchViewModel: NearbySearchViewModel) {
    val keyword = "clinic"
    val radius = 500
    val apiKey = "AIzaSyDZ7GHGvGgfAcldVbm8-zrM4Fghsds89CA"
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // State variables
    var location by remember { mutableStateOf("Unknown location") }
    var locationFetched by remember { mutableStateOf(false) }

    // Observe the clinics data
    val places = nearbySearchViewModel.places
    val bookmarkedClinics = nearbySearchViewModel.bookmarkedClinics
    var isExpanded by remember { mutableStateOf(false) }
    Log.d("Fetched Clinics", places.toString())

    // Text filter for clinics
    var searchQuery by remember { mutableStateOf("") }
    val filteredClinics = remember(searchQuery) {
        if (searchQuery.isBlank()) places else places.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val filteredBookmarkedClinics = remember(searchQuery) {
        if (searchQuery.isBlank()) bookmarkedClinics else bookmarkedClinics.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

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
            //nearbySearchViewModel.fetchNearbyPlaces(keyword, location, radius, apiKey)
            nearbySearchViewModel.fetchNearbyPlaces(keyword, location, radius, apiKey)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp, // Left padding
                    end = 16.dp,   // Right padding
                    top = paddingValues.calculateTopPadding(), // Keeps space for top UI
                    bottom = paddingValues.calculateBottomPadding() // Keeps space for bottom UI
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Clinics Test Page", fontSize = 32.sp)

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Search TextField
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Clinic") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bookmarked Clinics", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }

                    // bookmarked clinics
                    if (isExpanded) {
                        Box(
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val displayList = if (searchQuery.isBlank()) bookmarkedClinics else filteredBookmarkedClinics
                                // Display a loading message if clinics are empty
                                if (bookmarkedClinics.isEmpty()) {
                                    item {
                                        Text("No bookmarked clinics")
                                    }
                                } else {
                                    items(displayList, key = { it.place_id }) { place ->
                                        ClinicCard(place, navController, nearbySearchViewModel)
                                    }
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Card (
                    modifier = Modifier
                        .weight(1f) // Ensures this LazyColumn takes half of the available space
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        Text("Nearby Clinics", fontWeight = FontWeight.Bold, fontSize = 24.sp)

                        Row {
                            Button(
                                onClick = {
                                    // TODO: FILTER BUTTONS (open_now (t,f), radius (500, 1000 etc.), rating of clinic (1-5))

                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Filter")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Filter"
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    // TODO: Sort based on (rating, starting letter (default),
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sort")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Sort")
                            }
                        }
                    }

                    // Display the fetched clinic data in a LazyColumn
                    Box(

                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val displayList = if (searchQuery.isBlank()) places else filteredClinics
                            // Display a loading message if clinics are empty
                            if (places.isEmpty()) {
                                item {
                                    Text("Loading clinics data...")
                                }
                            } else {
                                // Use the 'items' function to iterate over places
                                items(displayList) { place ->
                                    ClinicCard(place, navController, nearbySearchViewModel)
                                }
                            }
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

@Composable
fun ClinicCard(place: Place, navController: NavController, nearbySearchViewModel: NearbySearchViewModel) {
    val isBookmarked by remember { derivedStateOf { nearbySearchViewModel.bookmarkStates[place.place_id] ?: false } }
    Log.d("poop", place.toString())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 120.dp)
            .clickable {
                val encodedClinicInfo =
                    Uri.encode("${place.name}|${place.vicinity}|${place.place_id}")
                navController.navigate("clinic/$encodedClinicInfo")
            },
        shape = RoundedCornerShape(12.dp), // Slightly rounded corners
        elevation = CardDefaults.elevatedCardElevation(4.dp) // Adds shadow effect
    ) {
        Row {

            // Sample image
            Image(
                painter = painterResource(id = R.drawable.sit_punggol),
                contentDescription = "Clinic Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.vicinity,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Bookmark Button
            IconButton(
                onClick = {
                    nearbySearchViewModel.toggleBookmark(place)
                }
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    }
}

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
