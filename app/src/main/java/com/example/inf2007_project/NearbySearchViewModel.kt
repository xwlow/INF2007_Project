package com.example.inf2007_project

import Place
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NearbySearchViewModel : ViewModel() {
    private val _places = mutableListOf<Place>()
    val places: List<Place> get() = _places

    // For debugging
    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(ApiNearbySearch::class.java)

    fun fetchNearbyPlaces(keyword: String, location: String, radius: Int, apiKey: String) {
        viewModelScope.launch {
            try {
                // Make the API call
                val response = retrofit.getNearbyPlaces(keyword, location, radius, apiKey)
                if (response.status == "OK") {
                    _places.clear()
                    _places.addAll(response.results)
                    Log.d("NEARBY", "Fetched places: ${_places.size}")
                } else {
                    Log.e("NEARBY", "API error: ${response.status} - ${response.errorMessage}")
                }
            } catch (e: Exception) {
                Log.e("NEARBY", "Network error: ${e.message}")
            }
        }
    }
}