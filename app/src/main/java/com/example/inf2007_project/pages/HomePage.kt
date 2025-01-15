package com.example.inf2007_project.pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.inf2007_project.AuthViewModel

@Composable
fun HomePage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel){
    Text(text = "This is the home page")
}