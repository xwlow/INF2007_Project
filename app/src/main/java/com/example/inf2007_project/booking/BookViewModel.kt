package com.example.inf2007_project.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inf2007_project.dependencies.DependencyData
import com.example.inf2007_project.dependencies.UserDetailData
import com.google.firebase.auth.FirebaseAuth
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

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames

    private val _dependenciesWithDetails = MutableStateFlow<List<Pair<DependencyData, UserDetailData>>>(emptyList())
    val dependenciesWithDetails: StateFlow<List<Pair<DependencyData, UserDetailData>>> = _dependenciesWithDetails

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

        // fetch list of dependencies
        db.collection("dependencies")
            .whereEqualTo("caregiverId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                val dependencyIds = documents.mapNotNull { doc ->
                    doc.getString("dependencyId") // Get dependencyId field
                }.toMutableList()

                Log.d("DependencyIDs", "Dependencies: $dependencyIds")

                // Always include the user's own ID in the list
                dependencyIds.add(user.uid)
                fetchConsultationsForDependencyIds(dependencyIds)

//                if (dependencyIds.isNotEmpty()) {
//                    // Fetch consultations for dependencies
//                    fetchConsultationsForDependencyIds(dependencyIds)
//                }
//                else {
//                    Log.d("DependencyIDs", "No dependencies found, fetching consultations for user")
//                    // Fetch consultations where dependencyId = userId
//                    fetchConsultationsForUser(user.uid)
//                }
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

    // Fetch names for consultations
    fun fetchUserNamesForConsultations() {
        val allDependencyIds = (pastConsultations.value + upcomingConsultations.value)
            .map { it.dependencyId }
            .filter { it.isNotEmpty() }
            .toSet() // Ensure uniqueness


        if (allDependencyIds.isEmpty()) return

        db.collection("userDetail")
            .whereIn("uid", allDependencyIds.toList())
            .get()
            .addOnSuccessListener { documents ->
                val userMap = documents.associate { doc ->
                    val userId = doc.getString("uid") ?: ""
                    val name = doc.getString("name") ?: "Unknown"
                    userId to name
                }
                _userNames.value = userMap
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching user details", e)
            }
    }

    fun fetchDependenciesWithDetails(userId: String) {
        val updatedList = mutableListOf<Pair<DependencyData, UserDetailData>>()

        val caregiverQuery = db.collection("dependencies").whereEqualTo("caregiverId", userId)
        val dependencyQuery = db.collection("dependencies").whereEqualTo("dependencyId", userId)

        caregiverQuery.addSnapshotListener { caregiverSnapshot, e ->
            if (e != null) {
                Log.e("Firestore Error", "Failed to listen for caregiver dependencies", e)
                return@addSnapshotListener
            }

            val caregiverDependencies = caregiverSnapshot?.documents?.mapNotNull { doc ->
                doc.toObject(DependencyData::class.java)?.copy(
                    dependencyId = doc.getString("dependencyId") ?: doc.id,
                    documentId = doc.id
                )
            } ?: emptyList()

            fetchUserDetailsForDependencies(caregiverDependencies, updatedList, "Caregiver") {
                dependencyQuery.addSnapshotListener { dependencySnapshot, e ->
                    if (e != null) {
                        Log.e("Firestore Error", "Failed to listen for dependency caregivers", e)
                        return@addSnapshotListener
                    }

                    val dependencyCaregivers = dependencySnapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(DependencyData::class.java)?.copy(
                            caregiverId = doc.getString("caregiverId") ?: doc.id,
                            documentId = doc.id
                        )
                    } ?: emptyList()

                    fetchUserDetailsForDependencies(dependencyCaregivers, updatedList, "User") {
                        _dependenciesWithDetails.value = updatedList.distinctBy { it.first.documentId }
                    }
                }
            }
        }
    }

    private fun fetchUserDetailsForDependencies(
        dependencies: List<DependencyData>,
        updatedList: MutableList<Pair<DependencyData, UserDetailData>>,
        userType: String, // Add userType here
        onComplete: () -> Unit
    ) {
        if (dependencies.isEmpty()) {
            onComplete()
            return
        }

        var fetchedCount = 0

        dependencies.forEach { dependency ->
            // Choose userId based on userType
            val userId = when (userType) {
                "Caregiver" -> dependency.dependencyId // Caregiver sees dependency's details
                "User" -> dependency.caregiverId // User sees caregiver's details
                else -> dependency.dependencyId // Default fall-back
            }

            if (userId != null) {
                db.collection("userDetail").document(userId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val userDetails = userDoc.toObject(UserDetailData::class.java)
                        if (userDetails != null) {
                            updatedList.add(dependency to userDetails)
                        }

                        fetchedCount++
                        if (fetchedCount == dependencies.size) {
                            _dependenciesWithDetails.value = updatedList.distinctBy { it.first.documentId }
                            onComplete()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore Error", "Failed to fetch user details", e)
                        fetchedCount++
                        if (fetchedCount == dependencies.size) {
                            onComplete()
                        }
                    }
            } else {
                // If userId is null for some reason, increment fetchedCount
                fetchedCount++
                if (fetchedCount == dependencies.size) {
                    onComplete()
                }
            }
        }
    }

}
