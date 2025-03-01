package com.example.inf2007_project

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inf2007_project.clinic.BookPage
import com.example.inf2007_project.clinic.BookViewModel
import com.example.inf2007_project.chatbot.ChatBotScreen
import com.example.inf2007_project.clinic.Booking
import com.example.inf2007_project.clinic.ClinicsDetail
import com.example.inf2007_project.clinic.ClinicsPage
import com.example.inf2007_project.clinic.ClinicsPageTest
import com.example.inf2007_project.clinic.NearbySearchViewModel
import com.example.inf2007_project.consultation.ConsultationsPage
import com.example.inf2007_project.message.ContactTest
import com.example.inf2007_project.notes.DetailPage
import com.example.inf2007_project.pages.HomePage
import com.example.inf2007_project.uam.LoginPage
import com.example.inf2007_project.message.Messaging
import com.example.inf2007_project.notes.NotesRemindersPage
import com.example.inf2007_project.uam.ProfilePage
import com.example.inf2007_project.clinic.QueuePage
import com.example.inf2007_project.clinic.QueueViewModel
import com.example.inf2007_project.clinic.SuccessBooking
import com.example.inf2007_project.consultation.ConsultationsPage2
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.uam.DependenciesPage
import com.example.inf2007_project.uam.SignupPage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, testViewModel: TestViewModel,
               nearbySearchViewModel: NearbySearchViewModel, queueViewModel: QueueViewModel,
               bookViewModel: BookViewModel){
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
        composable("clinicsTest"){
            ClinicsPage(modifier, navController, authViewModel, testViewModel)
        }
        composable("notes&reminders"){
            NotesRemindersPage(modifier, navController, authViewModel, testViewModel)
        }
        composable("messages/{recipientId}"){ backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
            Messaging(modifier, navController, recipientId)
        }

        composable("queue/{clinicInfo}") { backStackEntry ->
            val clinicInfo = backStackEntry.arguments?.getString("clinicInfo") ?: ""
            val (clinicName, clinicStreetName, clinicID) = clinicInfo.split("|")
            QueuePage(
                viewModel = queueViewModel,
                clinicName = clinicName ?: "Unknown Clinic",
                clinicStreetName = clinicStreetName,
                clinicID = clinicID,
                modifier = Modifier,
                navController = navController
            )
        }
        composable("clinics"){
            ClinicsPageTest(modifier, navController, authViewModel, testViewModel, nearbySearchViewModel)
        }

        // Single pages for the notes & documents
        composable("detail/{type}/{id}") { navBackStackEntry ->
            val type = navBackStackEntry.arguments?.getString("type") ?: "notes"
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            DetailPage(modifier, navController, authViewModel, testViewModel,type = type, id = id)
        }

        // Single pages for the clinics
        composable("clinic/{clinicInfo}") { backStackEntry ->
            val clinicInfo = backStackEntry.arguments?.getString("clinicInfo") ?: ""
            val (clinicName, clinicStreetName, clinicID) = clinicInfo.split("|")

            ClinicsDetail(
                viewModel = queueViewModel,
                clinicName = clinicName ?: "Unknown Clinic",
                clinicStreetName = clinicStreetName,
                clinicID = clinicID,
                modifier = Modifier,
                navController = navController
            )
        }

        // Consultation Booking
        composable("book/{clinicName}?consultationId={consultationId}") { backStackEntry ->
            val consultationId = backStackEntry.arguments?.getString("consultationId")
            val clinicName = backStackEntry.arguments?.getString("clinicName")
            var existingBooking by remember { mutableStateOf<Booking?>(null) }

            // Fetch existing consultation if consultationId is provided
            LaunchedEffect(consultationId) {
                if (consultationId != null) {
                    bookViewModel.getConsultation(consultationId) { booking ->
                        existingBooking = booking // Store the retrieved booking
                        Log.d("Bookings", existingBooking.toString())
                    }
                }
            }

            BookPage(
                clinicName = clinicName ?: "Unknown Clinic",
                modifier = Modifier,
                navController = navController,
                bookViewModel = bookViewModel,
                authViewModel = authViewModel,
                existingBooking = existingBooking
            )
        }

        // Success Consultation Booking
        composable("SuccessBooking/{bookingInfo}"){ backStackEntry ->
            val bookingInfo = backStackEntry.arguments?.getString("bookingInfo") ?: ""
            val (selectedDate, chosenTime) = bookingInfo.split("|")

            SuccessBooking(
                selectedDate = selectedDate,
                chosenTime = chosenTime,
                modifier = Modifier,
                navController = navController
            )
        }

        composable("contacts"){
            ContactTest(modifier, navController, authViewModel, testViewModel)
        }


        composable("consultations"){
            ConsultationsPage(modifier, navController, authViewModel, testViewModel)
        }

        composable("consultations2"){
            ConsultationsPage2(modifier, navController, bookViewModel)
        }

        composable("profile") {
            ProfilePage(modifier, navController, authViewModel)
        }

        composable("dependencies"){
            DependenciesPage(navController, authViewModel, bookViewModel)
        }

        composable("chatbot"){
            ChatBotScreen(modifier, navController)
        }

    } )
}
