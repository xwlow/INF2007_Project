package com.example.inf2007_project.uam

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Date
import java.util.Calendar
import java.util.*


@Composable
fun SignupPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cfmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var nric by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var DoB by remember { mutableStateOf("") }
    var mExpanded by remember { mutableStateOf(false) }

    val userRoles = listOf("User", "Caretaker")
    val icon = if (mExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val buttonEnable = email.isNotEmpty() && password.isNotEmpty() && cfmPassword.isNotEmpty() && name.isNotEmpty() && nric.isNotEmpty()

    val db = FirebaseFirestore.getInstance()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate("home")
                Toast.makeText(context, "You have successfully created an account!", Toast.LENGTH_SHORT).show()
            }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
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

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Signup Page", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(text = "Email") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(text = "Name") }, singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = nric, onValueChange = { nric = it }, label = { Text(text = "NRIC") }, singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))

        
        OutlinedTextField(
            value = contact,
            onValueChange = {
                if (it.length <= 8) {
                    contact = it
                } else {
                    Toast.makeText(context, "Contact number cannot be more than 8 characters", Toast.LENGTH_SHORT).show()
                }
            },
            label = { Text(text = "Contact Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Date Picker Input Field
        // Box to properly position the dropdown menu
        Box {
            OutlinedTextField(
                value = DoB,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    //.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cfmPassword,
            onValueChange = { cfmPassword = it },
            label = { Text(text = "Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown for User Role Selection
        Box {
            OutlinedTextField(
                value = userRole,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    //.fillMaxWidth()
                    .clickable { mExpanded = true },
                label = { Text("User Type") },
                trailingIcon = { Icon(icon, "Dropdown", Modifier.clickable { mExpanded = !mExpanded }) }
            )

            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false }
            ) {
                userRoles.forEach { label ->
                    DropdownMenuItem(
                        text = { Text(text = label) },
                        onClick = {
                            userRole = label
                            mExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val data = mutableMapOf<String, Any>()
            data["name"] = name
            data["nric"] = nric
            data["email"] = email
            data["phone"] = contact
            data["userRole"] = userRole
            data["DoB"] = DoB // Save Date of Birth to Firestore

            if (password == cfmPassword && password.length >= 6) {
                authViewModel.signup(email, password) { uid ->
                    if (uid != null) {
                        Log.d("Auth", "Successfully created user with UID: $uid")

                        data["uid"] = uid
                        db.collection("userDetail")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Account Created successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("Login")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error adding account: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.d("Auth", "Failed to create user")
                        Toast.makeText(context, "Failed to create user account", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (password != cfmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }, enabled = buttonEnable) {
            Text(text = "Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
