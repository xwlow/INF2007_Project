package com.example.inf2007_project.clinic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.R
import com.example.inf2007_project.pages.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessBooking(
    selectedDate: String,
    chosenTime: String,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card (
                shape = CircleShape,
                modifier = Modifier.size(150.dp)
                    .padding(bottom = 50.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = "Check",
                        modifier = Modifier
                            .size(50.dp) // Adjust size

                    )
                }
            }

            Text ("Success!",
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier
                    .padding(bottom = 30.dp)
            )

            Text ("You have successfully booked an appointment on",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                    .padding(bottom = 30.dp)
                    .widthIn(max = 220.dp))

            Text (selectedDate,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Text (chosenTime,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp)
        }
    }
}