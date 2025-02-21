package com.example.inf2007_project.clinic

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicsDetail(
    clinicName: String,
    clinicStreetName: String,
    clinicID: String,
    modifier: Modifier = Modifier,
    navController : NavController,
    viewModel: QueueViewModel
) {
    val isCheckingQueue = viewModel.isCheckingQueue
    val isAddingQueue = viewModel.isAddingQueue
    val hasQueue = viewModel.hasQueue
    val queueCount = viewModel.queueCount
    val checkQueueError = viewModel.checkQueueError
    val addQueueError = viewModel.addQueueError


    LaunchedEffect(clinicID) {
        viewModel.checkQueue(clinicID)
    }

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Placeholder for Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Gray)
            )

            // Address and Phone Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = clinicStreetName)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Phone", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = clinicID)
                }
            }

            // Services Provided Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Services Provided",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Queue and Waiting Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Queue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isCheckingQueue) {
                        Text(text = "Loading...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    } else if (checkQueueError != null) {
                        Text(
                            text = "Error",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    } else {
                        Text(
                            text = queueCount?.toString() ?: "wat",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Waiting Time", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "30 minutes", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                        if (currentUser != null) {
                            Log.d("ADDQUEUE", "logged in and added")
                            viewModel.addQueue(clinicID, currentUser)
                            val encodedClinicInfo = Uri.encode("${clinicName}|${clinicStreetName}|${clinicID}")
                            navController.navigate("queue/$encodedClinicInfo")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get Queue Number")
                }
                Button(
                    onClick = {
                        /* Handle book appointment */
                        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                        if (currentUser != null) {
                            navController.navigate("book/$clinicName")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Book an appointment")
                }
                Button(
                    onClick = { /* Handle call the clinic */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Call the clinic")
                }
            }
        }
    }
}
