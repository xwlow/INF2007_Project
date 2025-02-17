package com.example.inf2007_project.clinic

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthState
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.pages.BottomNavigationBar

@Composable
fun ClinicsPage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel){
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
            Text(text = "Clinics Page", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
