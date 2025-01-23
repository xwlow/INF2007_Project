package com.example.inf2007_project.pages

import android.os.Message
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.AuthState
import com.example.inf2007_project.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.testData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun HomePage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel){

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    var testField by remember {
        mutableStateOf("")
    }

    LaunchedEffect((authState.value)) {
        when(authState.value){
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
                Toast.makeText(context, "You have successfully signed out!", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
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
                .padding(paddingValues), // Padding to avoid overlapping with the bottom bar
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Test", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = testField,
                onValueChange = {
                    testField = it
                },
                label = {
                    Text(text = "Test Field")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val testData = testData(
                    testField = testField
                )
                testViewModel.saveData(testData, context)
            }) {
                Text(text = "Add Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Home Page", fontSize = 32.sp)

            TextButton(onClick = {
                authViewModel.signout()
            }) {
                Text(text = "Sign Out")
            }

            // Added a clinic test page with a test API to test my QueuePage - Deric
            TextButton(onClick = {
                navController.navigate("clinicsTest")
            }) {
                Text(text = "Clinics Test with test API Sample")
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Clinics", Icons.Filled.ThumbUp, "clinics"),
        BottomNavItem("Notes", Icons.Filled.ThumbUp, "notes&reminders"),
        BottomNavItem("Contacts", Icons.Filled.Call, "contacts"),
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
