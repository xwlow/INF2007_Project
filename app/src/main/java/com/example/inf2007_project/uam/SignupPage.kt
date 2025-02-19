package com.example.inf2007_project.uam

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cfmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var nric by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var mExpanded by remember { mutableStateOf(false) }

    val userRoles = listOf("User", "Caretaker")
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }

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

        // Box to properly position the dropdown menu
        Box {
            OutlinedTextField(
                value = userRole,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    //.fillMaxWidth()
                    .clickable { mExpanded = true }
                    .onGloballyPositioned { coordinates -> mTextFieldSize = coordinates.size.toSize() },
                label = { Text("User Type") },
                trailingIcon = { Icon(icon, "Dropdown", Modifier.clickable { mExpanded = !mExpanded }) }
            )

            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false },
                modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val data = mutableMapOf<String, Any>()
            data["name"] = name
            data["nric"] = nric
            data["email"] = email
            data["userRole"] = userRole

            if (password == cfmPassword && password.length >= 6) {
                // First sign up the user
                authViewModel.signup(email, password) { uid ->
                    if (uid != null) {
                        Log.d("Auth", "Successfully created user with UID: $uid")

                        // After successful authentication, add data to Firestore
                        // Add the UID to the data with the uid field
                        data["uid"] = uid

                        // Consider using the UID as the document ID instead of auto-generating one
                        db.collection("userDetail")
                            .document(uid)  // Use UID as document ID - best practice --> Primary Key
                            .set(data)      // Use set instead of add when document ID is specified
                            .addOnSuccessListener {
                                Toast.makeText(context, "Account Created successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("Login")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error adding account: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Signup failed
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
