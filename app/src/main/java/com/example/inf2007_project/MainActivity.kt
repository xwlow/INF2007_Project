package com.example.inf2007_project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.example.inf2007_project.clinic.BookViewModel
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
        val authViewModel: AuthViewModel by viewModels()
        val testViewModel: TestViewModel by viewModels()
        val nearbySearchViewModel: NearbySearchViewModel by viewModels()
        val queueViewModel: QueueViewModel by viewModels()
        val bookViewModel: BookViewModel by viewModels()

        setContent {
            INF2007_ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        testViewModel = testViewModel,
                        nearbySearchViewModel = nearbySearchViewModel,
                        queueViewModel = queueViewModel,
                        bookViewModel = bookViewModel
                    )
                }
            }
        }

        testFirestoreConnection(this)

        // Check Health Connect SDK availability and initialize
        checkHealthConnectAvailability(this)

    }

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
    )

    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            Log.d("HealthConnect", "All permissions granted.")
        } else {
            Log.e("HealthConnect", "Missing required permissions.")
        }
    }

    private fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        CoroutineScope(Dispatchers.IO).launch {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            withContext(Dispatchers.Main) {
                if (grantedPermissions.containsAll(PERMISSIONS)) {
                    Log.d("HealthConnect", "Permissions already granted. Proceeding...")
                } else {
                    requestPermissions.launch(PERMISSIONS)
                }
            }
        }
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

    private fun checkHealthConnectAvailability(context: Context) {
        val providerPackageName = "com.google.android.apps.healthdata" // Provider package for Health Connect

        // Check SDK status
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)

        when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                // Health Connect is unavailable on the device
                Log.e("HealthConnect", "Health Connect is unavailable.")
                Toast.makeText(context, "Health Connect is not available.", Toast.LENGTH_SHORT).show()
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                // Provider update is required
                Log.e("HealthConnect", "Provider update required for Health Connect.")
                redirectToProviderUpdate(context, providerPackageName)
            }
            else -> {
                // SDK is available, initialize HealthConnectClient
                initializeHealthConnectClient(context)
            }
        }
    }

    private fun redirectToProviderUpdate(context: Context, providerPackageName: String) {
        // Redirect user to the Google Play Store to update or install the provider
        val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.vending")
            data = Uri.parse(uriString)
            putExtra("overlay", true)
            putExtra("callerId", context.packageName)
        }
        context.startActivity(intent)
    }

    private fun initializeHealthConnectClient(context: Context) {
        try {
            val healthConnectClient = HealthConnectClient.getOrCreate(context)
            Log.d("HealthConnect", "Health Connect SDK is available and client initialized.")

            checkPermissionsAndRun(healthConnectClient)
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error initializing Health Connect client: ${e.message}")
        }
    }
}


