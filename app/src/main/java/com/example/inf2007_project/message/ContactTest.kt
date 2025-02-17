package com.example.inf2007_project.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ContactTest(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel) {
    //val db = FirebaseFirestore.getInstance()
    //val auth = FirebaseAuth.getInstance()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    RecipientHolder.recipientId = "Tw3KcTvaaON1QjJF03MPLy4VqsO2"
                    navController.navigate("messages")
                },
            )
            {
                Text(text = "Tw3KcTvaaON1QjJF03MPLy4VqsO2 (test@gmail.com)")
            }

            Button(
                onClick = {
                    RecipientHolder.recipientId = "kJTZpd8V9TR6uOrITEOs5MImGTR2"
                    navController.navigate("messages")
                },
            )
            {
                Text(text = "kJTZpd8V9TR6uOrITEOs5MImGTR2 (xw@gmail.com)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                RecipientHolder.recipientId = "hHEKnEyqvyfn5X68DIFfomyC2g33"
                navController.navigate("messages")
            })
            {
                Text(text = "hHEKnEyqvyfn5X68DIFfomyC2g33 (nric@test.com)")
            }
        }
    }

}

object RecipientHolder{
    var recipientId: String? = null
}
