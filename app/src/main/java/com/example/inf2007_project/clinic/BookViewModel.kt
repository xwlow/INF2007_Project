package com.example.inf2007_project.clinic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inf2007_project.uam.DependencyData
import com.example.inf2007_project.uam.UserDetailData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.components.Dependency
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Booking(
    val consultationId: String = "",
    val userId: String = "",
    val selectedDate: String = "",
    val doctorName: String = "",
    val chosenTime: String = "",
    val clinicName: String = "",
    val extraInformation: String = "",
    val dependencyId: String = "",
)

class BookViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pastConsultations = MutableStateFlow<List<Booking>>(emptyList())
    val pastConsultations: StateFlow<List<Booking>> = _pastConsultations

    private val _upcomingConsultations = MutableStateFlow<List<Booking>>(emptyList())
    val upcomingConsultations: StateFlow<List<Booking>> = _upcomingConsultations

    private val _availableSlots = MutableStateFlow<List<String>>(emptyList()) // Store available slots
    val availableSlots: StateFlow<List<String>> = _availableSlots

    init {
        // Observe FirebaseAuth user changes
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                Log.d("AuthListener", "User switched: ${currentUser.uid}")
                fetchConsultations()
            } else {
                Log.w("AuthListener", "User logged out, clearing data")
                _pastConsultations.value = emptyList()
                _upcomingConsultations.value = emptyList()
            }
        }
    }

    fun fetchConsultations() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("FirestoreError", "User not logged in")
            return
        }

        db.collection("dependencies")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                val dependencyIds = documents.mapNotNull { doc ->
                    doc.getString("dependencyId") // Get dependencyId field
                }

                Log.d("DependencyIDs", "Dependencies: $dependencyIds")

                if (dependencyIds.isNotEmpty()) {
                    // Fetch consultations for dependencies
                    fetchConsultationsForDependencyIds(dependencyIds)
                } else {
                    Log.d("DependencyIDs", "No dependencies found, fetching consultations for user")
                    // Fetch consultations where dependencyId = userId
                    fetchConsultationsForUser(user.uid)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Error", "Error getting dependencies", e)
            }
    }

    private fun fetchConsultationsForDependencyIds(dependencyIds: List<String>) {
        db.collection("consultations")
            .whereIn("dependencyId", dependencyIds)
            .addSnapshotListener { snapshot, e ->
                processConsultationResults(snapshot, e)
            }
    }

    private fun fetchConsultationsForUser(userId: String) {
        db.collection("consultations")
            .whereEqualTo("dependencyId", userId) // Fetch user's own bookings
            .addSnapshotListener { snapshot, e ->
                processConsultationResults(snapshot, e)
            }
    }

    private fun processConsultationResults(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.e("FirestoreError", "Query failed: ${e.message}")
            return
        }

        if (snapshot == null || snapshot.isEmpty) {
            Log.w("FirestoreWarning", "No matching consultations found.")
            _pastConsultations.value = emptyList()
            _upcomingConsultations.value = emptyList()
            return
        }

        val now = Calendar.getInstance().time

        val allConsultations = snapshot.documents.mapNotNull { doc ->
            Booking(
                consultationId = doc.id,
                userId = doc.getString("userId") ?: "",
                selectedDate = doc.getString("selectedDate") ?: "",
                doctorName = doc.getString("doctorName") ?: "",
                chosenTime = doc.getString("chosenTime") ?: "",
                clinicName = doc.getString("clinicName") ?: "",
                extraInformation = doc.getString("extraInformation") ?: "",
                dependencyId = doc.getString("dependencyId") ?: "",
            )
        }

        val pastList = mutableListOf<Booking>()
        val upcomingList = mutableListOf<Booking>()

        allConsultations.forEach { consultation ->
            val consultationDateTime = convertToDateTime(consultation.selectedDate, consultation.chosenTime)
            if (consultationDateTime != null) {
                if (consultationDateTime.before(now)) {
                    pastList.add(consultation)
                } else {
                    upcomingList.add(consultation)
                }
            }
        }

        val sortedPastList = pastList.sortedWith(compareBy(
            { convertToDateTime(it.selectedDate, it.chosenTime) },  // Sort by Date
            { it.chosenTime } // Then by Time
        ))

        val sortedUpcomingList = upcomingList.sortedWith(compareBy(
            { convertToDateTime(it.selectedDate, it.chosenTime) },
            { it.chosenTime }
        ))

        // Update StateFlow values
        _pastConsultations.value = sortedPastList
        _upcomingConsultations.value = sortedUpcomingList
    }


    // Function to fetch consultation timings
    fun fetchAvailableTimings(clinicName: String, selectedDate: String) {
        val user = auth.currentUser
        if (user == null) {
            Log.e("FirestoreError", "User not logged in")
            return
        }

        db.collection("consultations")
            .whereEqualTo("clinicName", clinicName)
            .whereEqualTo("selectedDate", selectedDate)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Query failed: ${e.message}")
                    return@addSnapshotListener
                }

                // Extract booked consultation timings
                val bookedTimings = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("chosenTime")
                } ?: emptyList()

                Log.d("FirestoreSuccess", "Booked timings: $bookedTimings")

                val allSlots = generateConsultationSlots(8, 17, 15) // 8 AM - 5 PM, 15 min interval
                val availableSlotsList = allSlots.filterNot { it in bookedTimings }

                Log.d("FirestoreSuccess", "Available slots: $availableSlotsList")

                _availableSlots.value = availableSlotsList
            }
    }

    // Function to save booking
    fun saveBooking(
        selectedDate: String,
        doctorName: String,
        chosenTime: String,
        clinicName: String,
        extraInformation: String,
        dependencyId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    onFailure(Exception("User not logged in"))
                    return@launch
                }

                // Create a Booking object WITHOUT consultationId
                val booking = hashMapOf(
                    "userId" to user.uid,
                    "selectedDate" to selectedDate,
                    "doctorName" to doctorName,
                    "chosenTime" to chosenTime,
                    "clinicName" to clinicName,
                    "extraInformation" to extraInformation,
                    "dependencyId" to dependencyId
                )

                FirebaseFirestore.getInstance()
                    .collection("consultations") // Top-level collection
                    .add(booking) // Firestore generates unique ID
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Update Booking
    fun updateBooking(
        bookingId: String,
        selectedDate: String,
        doctorName: String,
        chosenTime: String,
        clinicName: String,
        extraInformation: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updatedData = mapOf(
                    "selectedDate" to selectedDate,
                    "doctorName" to doctorName,
                    "chosenTime" to chosenTime,
                    "clinicName" to clinicName,
                    "extraInformation" to extraInformation
                )

                db.collection("consultations")
                    .document(bookingId) // Use existing document ID
                    .update(updatedData)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to delete booking
    fun deleteBooking(consultationId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("consultations")
                    .document(consultationId) // Deletes the document by its ID
                    .delete()
                    .await()

                onSuccess() // Call success callback
                Log.d("Firestore", "Successfully deleted consultation: $consultationId")
            } catch (e: Exception) {
                onFailure(e) // Call failure callback
                Log.e("Firestore", "Error deleting consultation", e)
            }
        }
    }

    // Function to convert date and time strings to a Date object
    private fun convertToDateTime(date: String, time: String): Date? {
        return try {
            val dateTimeString = "$date $time"
            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            formatter.parse(dateTimeString)
        } catch (e: Exception) {
            Log.e("DateConversion", "Error parsing date: $date $time", e)
            null
        }
    }

    // Find specific consultation
    fun getConsultation(consultationId: String, onSuccess: (Booking) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = db.collection("consultations").document(consultationId).get().await()
                val booking = doc.toObject(Booking::class.java)?.copy(consultationId = doc.id)
                booking?.let { onSuccess(it) }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Failed to fetch booking: $e")
            }
        }
    }

}
