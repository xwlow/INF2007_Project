package com.example.inf2007_project.clinicList

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ClinicsPageTest(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, nearbySearchViewModel: NearbySearchViewModel) {
    val keyword = "clinic"
    var radius by remember { mutableStateOf(500) }
    val apiKey = "AIzaSyDZ7GHGvGgfAcldVbm8-zrM4Fghsds89CA"
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // State variables
    var location by remember { mutableStateOf("Unknown location") }
    var locationFetched by remember { mutableStateOf(false) }

    // Observe the clinics data
    var places = nearbySearchViewModel.places
    val bookmarkedClinics = nearbySearchViewModel.bookmarkedClinics

    // Expanding btns
    var isExpandedBookmark by remember { mutableStateOf(false) }
    var isExpandedSort by remember { mutableStateOf(false) }
    var isExpandedFilter by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var sortOption by remember { mutableStateOf("DEFAULT") }
    Log.d("Fetched Clinics", places.toString())


    // Text filter for clinics
    var searchQuery by remember { mutableStateOf("") }

    var filteredClinics = remember(searchQuery) {
        if (searchQuery.isBlank()) places else places.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Log.d("filteredClinics", filteredClinics.toString())

    LaunchedEffect(places, sortOption) {
        places = when (sortOption) {
            "A-Z" -> nearbySearchViewModel.sortClinicsByNameAtoZ(places)
            "Z-A" -> nearbySearchViewModel.sortClinicsByNameZtoA(places)
            else -> nearbySearchViewModel.places
        }
        filteredClinics = when (sortOption) {
            "A-Z" -> nearbySearchViewModel.sortClinicsByNameAtoZ(filteredClinics)
            "Z-A" -> nearbySearchViewModel.sortClinicsByNameZtoA(filteredClinics)
            else -> places
        }
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
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Clinics", fontSize = 32.sp)

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
                            .clickable { isExpandedBookmark = !isExpandedBookmark }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bookmarked Clinics", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Icon(
                            imageVector = if (isExpandedBookmark) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpandedBookmark) "Collapse" else "Expand"
                        )
                    }

                    // bookmarked clinics
                    if (isExpandedBookmark) {
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
                            Box {
                                Button(
                                    onClick = {
                                        isExpandedFilter = true
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
                                DropdownMenu(
                                    expanded = isExpandedFilter,
                                    onDismissRequest = { isExpandedFilter = false }
                                ) {
                                    Text(
                                        text = "Clinics in vicinity:",
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Radius 500 meters") },
                                        onClick = {
                                            radius = 500
                                            nearbySearchViewModel.fetchNearbyPlaces(
                                                keyword,
                                                location,
                                                radius,
                                                apiKey
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Radius 1000 meters") },
                                        onClick = {
                                            radius = 1000
                                            nearbySearchViewModel.fetchNearbyPlaces(
                                                keyword,
                                                location,
                                                radius,
                                                apiKey
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Radius 1500 meters") },
                                        onClick = {
                                            radius = 1500
                                            nearbySearchViewModel.fetchNearbyPlaces(
                                                keyword,
                                                location,
                                                radius,
                                                apiKey
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Box {
                                Button(
                                    onClick = {
                                        isExpandedSort = true
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Sort By")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Sort"
                                    )
                                }
                                DropdownMenu(
                                    expanded = isExpandedSort,
                                    onDismissRequest = { isExpandedSort = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Clinic Name A-Z") },
                                        onClick = {
                                            places = nearbySearchViewModel.sortClinicsByNameAtoZ(places)
                                            filteredClinics = nearbySearchViewModel.sortClinicsByNameAtoZ(filteredClinics)
                                            isExpandedSort = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Clinic Name Z-A") },
                                        onClick = {
                                            places = nearbySearchViewModel.sortClinicsByNameZtoA(places)
                                            filteredClinics = nearbySearchViewModel.sortClinicsByNameZtoA(filteredClinics)
                                            isExpandedSort = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rating highest to lowest") },
                                        onClick = {
                                            places = nearbySearchViewModel.sortClinicsByRatingDescending(places)
                                            filteredClinics = nearbySearchViewModel.sortClinicsByRatingDescending(filteredClinics)
                                            isExpandedSort = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rating lowest to highest") },
                                        onClick = {
                                            places = nearbySearchViewModel.sortClinicsByRatingAscending(places)
                                            filteredClinics = nearbySearchViewModel.sortClinicsByRatingAscending(filteredClinics)
                                            isExpandedSort = false
                                        }
                                    )
                                }
                            }
                        }

                        // To position lazy column to the top of the list
                        LaunchedEffect(places) {
                            listState.scrollToItem(0)
                        }

                        // Display the fetched clinic data in a LazyColumn
                        Box(
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val displayList =
                                    if (searchQuery.isBlank()) places else filteredClinics
                                // Display a loading message if clinics are empty
                                if (places.isEmpty()) {
                                    item {
                                        Text("Loading clinics data...")
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
    //val isBookmarked by remember(place.place_id) { derivedStateOf { nearbySearchViewModel.bookmarkStates[place.place_id] ?: false } }
    Log.d("poop", place.toString())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .heightIn(min = 180.dp)
            .clickable {
                val encodedClinicInfo =
                    Uri.encode("${place.name}|${place.vicinity}|${place.place_id}")
                navController.navigate("clinic/$encodedClinicInfo")
            },
        shape = RoundedCornerShape(16.dp), // Slightly rounded corners
        elevation = CardDefaults.elevatedCardElevation(4.dp) // Adds shadow effect
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sample image
            Image(
                painter = painterResource(id = R.drawable.sit_punggol),
                contentDescription = "Clinic Image",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier
                    .height(4.dp)
                    .padding(top = 20.dp)
                )
                Text(
                    text = place.vicinity,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rating: ${place.rating}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // Bookmark Button
            IconButton(
                onClick = {
                    nearbySearchViewModel.toggleBookmark(place)
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.Gray,
//                    modifier = Modifier
//                        .offset(x= 12.dp)
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
