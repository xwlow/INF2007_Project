package com.example.inf2007_project.clinicList

import Place
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NearbySearchViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _places = mutableStateOf<List<Place>>(emptyList())
    val places: List<Place> get() = _places.value

    private val _bookmarkedClinics = mutableStateOf<List<Place>>(emptyList())
    val bookmarkedClinics: List<Place> get() = _bookmarkedClinics.value
    val bookmarkStates = mutableStateMapOf<String, Boolean>()

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

//    fun fetchNearbyPlaces(keyword: String, location: String, radius: Int, apiKey: String) {
//        viewModelScope.launch {
//            try {
//                // Make the API call
//                val response = retrofit.getNearbyPlaces(keyword, location, radius, apiKey)
//                if (response.status == "OK") {
//                    _places.value = response.results
//                    Log.d("NEARBY", "Fetched places: ${_places.value.size}")
//                } else {
//                    Log.e("NEARBY", "API error: ${response.status} - ${response.errorMessage}")
//                }
//            } catch (e: Exception) {
//                Log.e("NEARBY", "Network error: ${e.message}")
//            }
//        }
//    }

    fun fetchNearbyPlaces(keyword: String, location: String, radius: Int, apiKey: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    Log.d("User", "No user logged in")
                    return@launch
                }

//                val bookmarkedClinics = mutableSetOf<String>() // Store placeIds of bookmarked clinics
                val bookmarkedClinicsSet = mutableSetOf<String>()
                val bookmarkedClinicsList = mutableListOf<Place>()

                val snapshot = db.collection("bookmarked")
                    .whereEqualTo("userId", user.uid)
                    .get()
                    .await()

                for (document in snapshot.documents) {
                    val placeId = document.getString("placeId")
                    val name = document.getString("clinicName") ?: "Unknown Clinic"
                    val vicinity = document.getString("vicinity") ?: "Unknown Location"
                    val rating = document.getDouble("rating") ?: 0.0

                    if (placeId != null) {
                        bookmarkedClinicsSet.add(placeId)
                        bookmarkedClinicsList.add(
                            Place(
                                name,
                                vicinity,
                                placeId,
                                isBookmarked = true,
                                rating
                            )
                        )

                        bookmarkStates[placeId] = true
                    }
                }

                // Update bookmarked clinics state
                _bookmarkedClinics.value = bookmarkedClinicsList

                Log.d("Fetched Clinics", bookmarkedClinics.toString())

                val response = retrofit.getNearbyPlaces(keyword, location, radius, apiKey)
                if (response.status == "OK") {
                    val placesWithBookmarks = response.results.map { place ->
                        place.copy(isBookmarked = bookmarkedClinicsSet.contains(place.place_id))
                    }

                    _places.value = placesWithBookmarks // Update UI with bookmark status
                    Log.d(
                        "NEARBY",
                        "Fetched places: ${_places.value.size}, Bookmarked: ${bookmarkedClinics.size}"
                    )
                } else {
                    Log.e("NEARBY", "API error: ${response.status} - ${response.errorMessage}")
                }

            } catch (e: Exception) {
                Log.e("NEARBY", "Error fetching bookmarks or places: ${e.message}")
            }
        }
    }

    fun toggleBookmark(place: Place) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val placeId = place.place_id

            if (bookmarkStates[placeId] == true) {
                db.collection("bookmarked").document("${user.uid}-$placeId").delete().await()
                bookmarkStates[placeId] = false

            } else {
                val clinicData = mapOf(
                    "userId" to user.uid,
                    "clinicName" to place.name,
                    "vicinity" to place.vicinity,
                    "placeId" to placeId
                )
                db.collection("bookmarked").document("${user.uid}-$placeId").set(clinicData).await()
                bookmarkStates[placeId] = true

                _bookmarkedClinics.value = _bookmarkedClinics.value + place.copy(isBookmarked = true)
            }

            _places.value = _places.value.map {
                if (it.place_id == placeId) it.copy(isBookmarked = bookmarkStates[placeId] ?: false)
                else it
            }

            _bookmarkedClinics.value = _bookmarkedClinics.value.filter {
                bookmarkStates[it.place_id] == true
            }
        }
    }

    fun sortClinicsByNameAtoZ(filteredClinics: List<Place>): List<Place> {
        return filteredClinics.sortedBy { it.name.lowercase() }
    }

    fun sortClinicsByNameZtoA(filteredClinics: List<Place>): List<Place> {
        return filteredClinics.sortedByDescending { it.name.lowercase() }
    }

    fun sortClinicsByRatingDescending(clinics: List<Place>): List<Place> {
        return clinics.sortedByDescending { it.rating ?: 0.0 }
    }

    fun sortClinicsByRatingAscending(clinics: List<Place>): List<Place> {
        return clinics.sortedBy { it.rating ?: 0.0 }
    }

}

