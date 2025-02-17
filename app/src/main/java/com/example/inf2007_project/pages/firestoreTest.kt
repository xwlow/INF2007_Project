package com.example.inf2007_project.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthState
import com.example.inf2007_project.uam.AuthViewModel

@Composable
fun firestoreTest(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel){

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    var email by remember {
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

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Login Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
        },
            label = {
                Text(text = "Email")
            })

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Home Page", fontSize = 32.sp)

        TextButton(onClick = {
            authViewModel.signout()
            //Toast.makeText(context, "You have successfully signed out!", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "Sign Out")
        }
    }


}