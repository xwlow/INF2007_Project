package com.example.inf2007_project.clinic

import android.util.Log
import androidx.lifecycle.ViewModel
//import com.example.inf2007_project.model.ApiResponse
//import com.example.inf2007_project.model.Clinic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClinicViewModel : ViewModel() {
    // Store the list of clinics as LiveData or State
    private val _clinics = mutableListOf<ClinicResponseModels>()
    val clinics: List<ClinicResponseModels> get() = _clinics

    // Function to fetch data from the API
    fun fetchClinicsData() {
        // Create Retrofit instance with the custom client
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.gov.sg/api/action/")
            .build()
            .create(ApiInterfaceClinic::class.java)

        // Make the API call
        val retrofitData = retrofitBuilder.getData()
        retrofitData.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                val responseBody = response.body()?.result?.records ?: return

                _clinics.clear() // Clear any previous data
                _clinics.addAll(responseBody) // Add the new data to the list

                //Log.d("YEET", "Fetched clinics: ${_clinics.size}")
                } else {
                    Log.e("CLINIC", "API error: ${response.code()} - ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("CLINIC", "Error fetching data", t)
            }
        })
    }
}
