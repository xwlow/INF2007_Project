package com.example.inf2007_project.uam

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun updateProfile(userId: String, name: String, phone: String) {
        val userRef = db.collection("users").document(userId)
        val userData = hashMapOf(
            "name" to name,
            "phone" to phone
        )

        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Profile updated successfully in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error updating profile", e)
            }
    }
}