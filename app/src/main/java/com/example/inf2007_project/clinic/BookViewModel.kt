package com.example.inf2007_project.clinic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Booking(
    val userId: String = "",
    val selectedDate: String = "",
    val doctorName: String = "",
    val chosenTime: String = "",
    val clinicName: String = "",
    val extraInformation: String = ""
)

class BookViewModel : ViewModel() {

    // Function to save booking
    fun saveBooking(
        selectedDate: String,
        doctorName: String,
        chosenTime: String,
        clinicName: String,
        extraInformation: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    onFailure(Exception("User not logged in"))
                    return@launch
                }

                val booking = Booking(
                    userId = user.uid,
                    selectedDate = selectedDate,
                    doctorName = doctorName,
                    chosenTime = chosenTime,
                    clinicName = clinicName,
                    extraInformation = extraInformation
                )

//                FirebaseFirestore.getInstance().collection("consultations")
//                    .document(user.uid) // Use userID as the document ID (Primary Key)
//                    .set(booking)
//                    .await() // Use suspend function to wait for Firestore response

//                FirebaseFirestore.getInstance()
//                    .collection("bookings") // Top-level collection
//                    .document(user.uid) // Each user has a document
//                    .collection("consultations")
//                    .add(booking) // Firestore generates unique ID
//                    .await()

                FirebaseFirestore.getInstance()
                    .collection("bookings") // Top-level collection
                    .document(user.uid) // Each user has a document
                    .collection("consultations")
                    .add(booking) // Firestore generates unique ID
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
