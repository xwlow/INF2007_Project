package com.example.inf2007_project.uam

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.Calendar


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

    //for observing firestore user details
    val userDetails by profileViewModel.userDetails.observeAsState()

    //for user details
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf( "") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var DoB by remember { mutableStateOf("") }
    var nric by remember { mutableStateOf("") }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
                Toast.makeText(context, "Profile Page", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserDetails()
    }

    LaunchedEffect(userDetails) {
        userDetails?.let {
            name = it.name
            email = it.email
            phone =  it.phone
            DoB = it.dob
            nric = it.nric
        }
    }

    // Function to open the Date Picker
    fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            DoB = "$selectedDay/${selectedMonth + 1}/$selectedYear" // Format Date
        }, year, month, day).show()


        //Log.d("Age", age.toString())
    }

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
            horizontalAlignment = Alignment.Start
        ) {

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =  Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Page",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                //add, view and delete b dependencies button here
//                Button(
//                    onClick = {
//                        navController.navigate("dependencies") },
//                    modifier = Modifier.width(180.dp)
//                ) {
//                    Text("Dependencies")
//                }
            }

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
                        //enabled = true,
                        label = { Text("Email") },
                        //readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nric,
                        onValueChange = {nric = it },
                        enabled = false,
                        label = { Text("NRIC") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 8) {
                                phone = it
                            } else {
                                Toast.makeText(context, "Contact number cannot be more than 8 characters", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text(text = "Contact Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box {
                        OutlinedTextField(
                            value = DoB,
                            onValueChange = {DoB = it},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openDatePicker() },
                            label = { Text("Date of Birth") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = "Select Date",
                                    modifier = Modifier.clickable { openDatePicker() }
                                )
                            }
                        )
                    }



                    //for editing button
                    Button(
                        onClick = {
                            // Save profile changes
                            profileViewModel.updateProfile(name, email, phone, DoB, nric)
                            //updateFirebaseAuthProfile(name, phone)
                            profileViewModel.saveUserInfo()
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update")
                    }

                    //User Signout Function

                    Button(
                        onClick = {
                            authViewModel.signout()
                            Toast.makeText(context, "User successfully signed out!", Toast.LENGTH_SHORT).show()

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Out")
                    }

                    //for deleting account button
                    Button(
                        onClick = {
                            profileViewModel.deleteProfile(authViewModel)
                            Toast.makeText(context, "User deleted!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}



