package com.example.inf2007_project.pages

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inf2007_project.AuthViewModel
import com.example.inf2007_project.ClinicViewModel
import com.example.inf2007_project.TestViewModel

@Composable
fun ClinicsPageTest(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel, clinicViewModel: ClinicViewModel) {

    // Fetch clinics data when the composable is launched
    LaunchedEffect(Unit) {
        clinicViewModel.fetchClinicsData()
    }

    // Observe the clinics data
    val clinics = clinicViewModel.clinics

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Padding to avoid overlapping with the bottom bar
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Clinics Test Page", fontSize = 32.sp)

            // Display the fetched clinic data in a LazyColumn
            LazyColumn (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display a loading message if clinics are empty
                if (clinics.isEmpty()) {
                    item {
                        Text("Loading clinics data...")
                    }
                } else {
                    // Use the 'items' function to iterate over clinics
                    items(clinics) { clinic ->
                        Button(
                            onClick = {
                                // Format values to be sent over to QueuePage
                                var streetName = "BLK" + clinic.block + " " + clinic.street_name
                                if (clinic.floor_number != null && clinic.unit_number != null) {
                                    streetName += " #" + clinic.floor_number + "-" + clinic.unit_number
                                }
                                val postalCode = "SINGAPORE " + clinic.postal_code
                                // Encode the concatenated string
                                val encodedClinicInfo = Uri.encode("${clinic.name}|${streetName}|${postalCode}")
                                // Values from Clinic Api to QueuePage
                                navController.navigate("queue/$encodedClinicInfo")
                                //navController.navigate("clinicDetail/${Uri.encode(clinic.name)}")
                            },
                        ) {
                            Text(text = clinic.name)
                        }
                    }
                }
            }
        }
    }
}
