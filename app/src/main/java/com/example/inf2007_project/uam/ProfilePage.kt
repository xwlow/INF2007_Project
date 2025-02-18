package com.example.inf2007_project.uam

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest


@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    //for user details
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }

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
                        //value = currentUser.displayName ?: "Not Available",
                        value = name,
                        onValueChange = {name = it },
                        label = { Text("Name") },
                        //readOnly = !isEditing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        //value = currentUser.email ?: "Not Available",
                        value = email,
                        onValueChange = {email = it },
                        label = { Text("Email") },
                        //readOnly = !isEditing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        //value = currentUser.phoneNumber ?: "Not Available",
                        value = phone,
                        onValueChange = {phone = it },
                        label = { Text("Phone") },
                        //readOnly = !isEditing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    //for editing button
                    Button(
                        onClick = {
                            // Save profile changes
                            currentUser?.uid?.let { userId ->
                                profileViewModel.updateProfile(userId, name, phone)
                                updateFirebaseAuthProfile(name, email)
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

fun updateFirebaseAuthProfile(name: String, email: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    //for update profile
    currentUser?.updateProfile(
        UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
    )?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Profile Update", "User profile updated successfully")
        } else {
            Log.e("Profile Update", "Failed to update user profile", task.exception)
        }
    }

    //for update email
    email?.let {
        currentUser?.updateEmail(it)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Profile Update", "Email updated successfully")
            } else {
                Log.e("Profile Update", "Failed to update email", task.exception)
            }
        }
    }
}
