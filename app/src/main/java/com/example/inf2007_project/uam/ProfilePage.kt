package com.example.inf2007_project.uam

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
                Toast.makeText(context, "Profile Page", Toast.LENGTH_SHORT).show()
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Profile Page",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (currentUser == null) {
                Text("User data not available. Please log in.")
            } else {

                // row for image here
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = currentUser.displayName ?: "Not Available",
                        onValueChange = {},
                        label = { Text("Name") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentUser.email ?: "Not Available",
                        onValueChange = {},
                        label = { Text("Email") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentUser.phoneNumber ?: "Not Available",
                        onValueChange = {},
                        label = { Text("Phone") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
