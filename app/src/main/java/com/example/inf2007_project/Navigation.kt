package com.example.inf2007_project

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inf2007_project.pages.ClinicsPage
import com.example.inf2007_project.pages.HomePage
import com.example.inf2007_project.pages.LoginPage
import com.example.inf2007_project.pages.Messaging
import com.example.inf2007_project.pages.NotesRemindersPage
import com.example.inf2007_project.pages.QueuePage
import com.example.inf2007_project.pages.SignupPage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, testViewModel: TestViewModel){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder =  {
        composable("login"){
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup"){
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home"){
            HomePage(modifier, navController, authViewModel, testViewModel)
        }
        composable("clinics"){
            ClinicsPage(modifier, navController, authViewModel, testViewModel)
        }
        composable("notes&reminders"){
            NotesRemindersPage(modifier, navController, authViewModel, testViewModel)
        }
        composable("messages"){
            Messaging(modifier, navController, authViewModel, testViewModel)
        }
        composable("queue"){
            QueuePage(modifier, navController, authViewModel, testViewModel)
        }
        
    } )
}
