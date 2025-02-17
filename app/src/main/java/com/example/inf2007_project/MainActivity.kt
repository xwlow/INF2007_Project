package com.example.inf2007_project

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.inf2007_project.clinic.NearbySearchViewModel
import com.example.inf2007_project.clinic.QueueViewModel
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.ui.theme.INF2007_ProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val testViewModel: TestViewModel by viewModels()
        val nearbySearchViewModel: NearbySearchViewModel by viewModels()
        val queueViewModel: QueueViewModel by viewModels()
        setContent {
            INF2007_ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel, testViewModel = testViewModel, nearbySearchViewModel = nearbySearchViewModel, queueViewModel = queueViewModel)
                }
            }
        }
        testFirestoreConnection(this)
    }


    private fun testFirestoreConnection(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        val fireStoreRef = Firebase.firestore
            .collection("test")
            .document("connection_test")



        try {
            fireStoreRef.set(mapOf("status" to "connected")).await()
            val snapshot = fireStoreRef.get().await()
            val status = snapshot.getString("status")

            withContext(Dispatchers.Main) {
                if (status == "connected") {
                    Toast.makeText(context, "Firestore connection successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Firestore connection failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

