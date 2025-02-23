package com.example.inf2007_project.uam

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userDetails = MutableLiveData<UserDetails?>()
    val userDetails: LiveData<UserDetails?> = _userDetails

    // to be activated in login screen
    fun getUserUID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?:""
    }

    fun saveUserInfo() {
        val userUID = getUserUID()
        if (userUID.isEmpty()) return

        val userInfoRef = db.collection("userDetail").document(userUID)
        val userInfoData = hashMapOf(
            "userUID" to userUID,
            "name" to "",
            "phone" to ""
        )
    }

    fun fetchUserDetails() {
//        val email = auth.currentUser?.email ?: return
//        Log.d("Firestore Debug", "Fetched Email: $email")
//
//        db.collection("userDetail").document(email).get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val name = document.getString("name") ?: ""
//                    val phone = document.getString("phone") ?: ""
//                    _userDetails.value = UserDetails(name, phone)
//                    Log.d("ProfileViewModel", "User details fetched: $name, $phone")
//                } else {
//                    Log.d("ProfileViewModel", "No user data found")
//                    _userDetails.value = UserDetails("", "")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("ProfileViewModel", "Failed to fetch user details", e)
//                _userDetails.value = UserDetails("", "")
//            }
        val userUID = getUserUID()
        if (userUID.isEmpty()) return

        val userInfoRef = db.collection("userDetail").document(userUID)

        //fetch user data from firestore
        userInfoRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val dob = document.getString("DoB") ?: ""
                    _userDetails.value = UserDetails(name, email, phone, dob)
                    Log.d("ProfileViewModel", "User details fetched: $name, $email,$phone, $dob for UID: $userUID")
                } else {
                    Log.d("ProfileViewModel", "No user data found")
                    _userDetails.value = UserDetails("", "", "", "")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Failed to fetch user details", e)
                _userDetails.value = UserDetails("", "", "", "")
            }
        }

        fun updateProfile(name: String, email: String, phone: String, dob: String) {
            val userUID = getUserUID()
            if (userUID.isEmpty()) return

            val userInfoRef = db.collection("userDetail").document(userUID)
            val updateData = hashMapOf(
                //"userUID" to userUID,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "DoB" to dob
            )

            userInfoRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "Profile updated successfully in Firestore")
                    _userDetails.value = UserDetails(name, email, phone, dob)
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error updating profile", e)
                }
        }

        fun deleteProfile() {
            val user = auth.currentUser
            val userUID = getUserUID()
            if (userUID.isEmpty()) return

            val userInfoRef = db.collection("userDetail").document(userUID)
            userInfoRef?.delete()
            user?.delete()
                ?.addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "User account deleted successfully.")
                    } else {
                        Log.w("Firebase", "Error deleting user account", task.exception)
                    }
                }
        }
    }


//fun updateFirebaseAuthProfile(name: String, phone: String) {
//    val currentUser = FirebaseAuth.getInstance().currentUser
//
//    //for update profile
//    currentUser?.updateProfile(
//        UserProfileChangeRequest.Builder()
//            .setDisplayName(name)
//            .build()
//    )?.addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//            Log.d("Profile Update", "User profile updated successfully")
//        } else {
//            Log.e("Profile Update", "Failed to update user profile", task.exception)
//        }
//    }
//}

data class UserDetails(val name: String, val email: String, val phone: String, val dob: String)
