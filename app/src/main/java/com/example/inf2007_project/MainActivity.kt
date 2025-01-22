package com.example.inf2007_project

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.inf2007_project.ui.theme.INF2007_ProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.StringBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val testViewModel: TestViewModel by viewModels()
        val clinicViewModel: ClinicViewModel by viewModels()
        setContent {
            INF2007_ProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel, testViewModel = testViewModel, clinicViewModel = clinicViewModel)
                }
            }
        }
        testFirestoreConnection(this)
        //getMyData()
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

//    private fun getMyData() {
//        val retrofitBuilder = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl("https://data.gov.sg/api/action/")
//            .build()
//            .create(ApiInterfaceClinic::class.java)
//
//        val retrofitData = retrofitBuilder.getData()
//        Log.d("YEET", "NIHAO")
//        retrofitData.enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(
//                call: Call<ApiResponse>,
//                response: Response<ApiResponse>
//            ) {
//                val responseBody = response.body()?.result?.records ?: return
//
//                val myStringBuilder = StringBuilder()
//                for (myData in responseBody) {
//                    myStringBuilder.append(myData.name)
//                    myStringBuilder.append("\n")
//                }
//                Log.d("YEET", myStringBuilder.toString())
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                Log.d("YEET", "Error Message: " + t.message)
//            }
//        })
//    }
}

