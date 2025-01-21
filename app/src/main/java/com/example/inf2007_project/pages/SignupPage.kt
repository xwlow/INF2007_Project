package com.example.inf2007_project.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.AuthState
import com.example.inf2007_project.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupPage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel){
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var cfmPassword by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var nric by remember {
        mutableStateOf("")
    }
    val db = FirebaseFirestore.getInstance()
    //val auth = FirebaseAuth.getInstance()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value){
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
    ){
        Text(text = "Signup Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
        },
            label = {
                Text(text = "Email")
            })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = password, onValueChange = {
            password = it
        },
            label = {
                Text(text = "Password")
            },
            visualTransformation = PasswordVisualTransformation(), // Masks the input
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = cfmPassword, onValueChange = {
            cfmPassword = it
        },
            label = {
                Text(text = "Confirm Password")
            },
            visualTransformation = PasswordVisualTransformation(), // Masks the input
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = name, onValueChange = {
            name = it
        },
            label = {
                Text(text = "Name")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = nric, onValueChange = {
            nric = it
        },
            label = {
                Text(text = "NRIC")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val data = mutableMapOf<String, Any>()

//          data["category"] = selectedCategory
            data["name"] = name
            data["nric"] = nric
            data["email"] = email
            if (password == cfmPassword && password.length >= 6) {
                authViewModel.signup(email, password)
                db.collection("userDetail")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Account Created successfully!", Toast.LENGTH_SHORT).show()
                        navController.navigate("Login")
                    }
                    .addOnFailureListener{e ->
                        Toast.makeText(context, "Error adding account: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

//                Toast.makeText(context, "Account Successfully Created!", Toast.LENGTH_SHORT).show()
//                navController.navigate("Login")
            }
            else if(password != cfmPassword){
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "Please ensure that your password is minimally 6 characters", Toast.LENGTH_SHORT).show()
            }
            //navController.navigate("login")
        }) {
            Text(text = "Create Account")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}