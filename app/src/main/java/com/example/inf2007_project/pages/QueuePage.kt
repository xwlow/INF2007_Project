package com.example.inf2007_project.pages

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.QueueViewModel
import com.example.inf2007_project.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QueuePage(
    viewModel: QueueViewModel,
    clinicName: String,
    clinicStreetName: String,
    clinicID: String,
    modifier: Modifier = Modifier,
    navController : NavController,
) {
    val isCheckingQueue = viewModel.isCheckingQueue
    val isAddingQueue = viewModel.isAddingQueue
    val hasQueue = viewModel.hasQueue
    val queueCount = viewModel.queueCount
    val checkQueueError = viewModel.checkQueueError
    val addQueueError = viewModel.addQueueError

    LaunchedEffect(Unit) {
        viewModel.checkQueue(clinicID)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            //Place Holder
            Card(
                modifier = Modifier
                    .padding(8.dp), // Adds padding around the card
                elevation = CardDefaults.elevatedCardElevation(4.dp), // Adds shadow/elevation to make it look like a card
            ) {
                Text(
                    "NUMBER: 3012",
                    modifier = Modifier
                        .padding(16.dp) // Adds padding inside the card
                        .align(Alignment.CenterHorizontally), // Centers the text horizontally inside the card
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (isCheckingQueue) {
                Text(text = "Loading...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            } else if (checkQueueError != null) {
                Text(
                    text = "Error",
                    fontSize = 50.sp, // Increase the size of the text
                    fontWeight = FontWeight.Bold, // Make the text bold
                )
            } else {
                Text(
                    text = queueCount?.toString() ?: "0",
                    fontSize = 50.sp, // Increase the size of the text
                    fontWeight = FontWeight.Bold, // Make the text bold
                )
            }

            Text("People are currently ahead of you")

            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        top = 20.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    ), // Apply padding only to the Card itself
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth() // Reset modifier for the Column
                ) {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = 8.dp,
                                end = 8.dp
                            ), // Padding only on the left and right
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clinicName,
                            modifier = Modifier
                                .widthIn(max = 250.dp) // Set a max width
                                .padding(end = 8.dp), // Add spacing between Text and Button
                            maxLines = 1, // Restrict to a single line
                            overflow = TextOverflow.Ellipsis // Truncate the text with ellipsis if it's too long
                        )

                        Spacer(modifier = Modifier.weight(1f)) // Pushes the Button to the right

                        Button(
                            onClick = {
                                val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                                if (currentUser != null) {
                                    viewModel.deleteUserFromQueue(clinicID, currentUser)
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red, // Set the background color to red
                                contentColor = Color.White // Set the text color to white (optional)
                            )
                        ) {
                            Text(text = "Cancel")
                        }

                    }
                    // Sample image
                    Image(
                        painter = painterResource(id = R.drawable.sit_punggol),
                        contentDescription = "Clinic Image",
                        modifier = Modifier
                            .size(width = 400.dp, height = 100.dp), // Set custom width and height
                        contentScale = ContentScale.Crop // Ensure the image is cropped to fit the specified size
                    )

                    Row {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(
                                    width = 200.dp,
                                    height = 150.dp
                                ), // Adds padding around the card
                            elevation = CardDefaults.elevatedCardElevation(4.dp), // Add shadow
                            shape = RoundedCornerShape(8.dp) // Add rounded corners
                        ) {
                            Text("Address:")
                            Text(clinicStreetName)
                            Text("No postal code for this api sadly")
                        }

                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(
                                    width = 150.dp,
                                    height = 150.dp
                                ), // Adds padding around the card
                            elevation = CardDefaults.elevatedCardElevation(4.dp), // Add shadow
                            shape = RoundedCornerShape(8.dp) // Add rounded corners
                        ) {
                            Text("Phone:")
                            Text("+65 8823 3412")
                        }

                    }
                }
            }
        }
    }
}
