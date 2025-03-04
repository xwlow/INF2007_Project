package com.example.inf2007_project.uam

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    private val _userDetails = MutableLiveData<UserDetails?>()
    val userDetails: LiveData<UserDetails?> = _userDetails

    // to be activated in login screen
    fun getUserUID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                    val nric = document.getString("nric") ?: ""
                    _userDetails.value = UserDetails(name, email, phone, dob, nric)
                    Log.d(
                        "ProfileViewModel",
                        "User details fetched: $name, $email,$phone, $dob, $nric for UID: $userUID"
                    )
                } else {
                    Log.d("ProfileViewModel", "No user data found")
                    _userDetails.value = UserDetails("", "", "", "", "")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Failed to fetch user details", e)
                _userDetails.value = UserDetails("", "", "", "", "")
            }
    }

    fun updateProfile(name: String, email: String, phone: String, dob: String, nric: String, authViewModel: AuthViewModel, context: Context) {
        val userUID = getUserUID()
        if (userUID.isEmpty()) return

        val userInfoRef = db.collection("userDetail").document(userUID)
        val currentUser = auth.currentUser
        val currentEmail = currentUser?.email ?: ""

        // If email is changing, handle it specially
        if (email != currentEmail && currentUser != null) {
            // First update all non-email fields
            val nonEmailUpdateData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "DoB" to dob,
                "nric" to nric
            )

            userInfoRef.set(nonEmailUpdateData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "Non-email fields updated successfully in Firestore")

                    // Now handle email verification
                    currentUser.verifyBeforeUpdateEmail(email)
                        .addOnSuccessListener {
                            Log.d("ProfileViewModel", "Verification email sent to $email")

                            // Set up a listener for auth state changes
                            val authStateListener = object : FirebaseAuth.AuthStateListener {
                                override fun onAuthStateChanged(auth: FirebaseAuth) {
                                    val user = auth.currentUser
                                    if (user != null && user.email == email) {
                                        // Email has been verified and updated
                                        // Update email in Firestore
                                        val emailUpdateData = hashMapOf("email" to email)
                                        userInfoRef.set(emailUpdateData, SetOptions.merge())
                                            .addOnSuccessListener {
                                                Log.d("ProfileViewModel", "Email updated in Firestore after verification")
                                                _userDetails.value = UserDetails(name, email, phone, dob, nric)
                                                // Remove the listener since we're done
                                                auth.removeAuthStateListener(this)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("ProfileViewModel", "Error updating email in Firestore", e)
                                                // Remove the listener even on failure
                                                auth.removeAuthStateListener(this)
                                            }
                                    }
                                }
                            }
                            authViewModel.signout()
                            Toast.makeText(context, "Please check inbox, verify and login again with new updated email", Toast.LENGTH_LONG).show()
                            // Add the auth state listener
                            auth.addAuthStateListener(authStateListener)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileViewModel", "Failed to send verification email", e)
                            Toast.makeText(context, "Verification Email failed to send, please re-login and try again!", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error updating non-email fields", e)
                }
        } else {
            // If email is not changing, update everything at once
            val updateData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "DoB" to dob,
                "nric" to nric
            )

            userInfoRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "Profile updated successfully in Firestore")
                    _userDetails.value = UserDetails(name, email, phone, dob, nric)
                    Toast.makeText(context, "Profile has been successfully updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error updating profile", e)
                    Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                }



        }
    }

    fun deleteProfile(authViewModel: AuthViewModel, context: Context, onAccountDeleted: () -> Unit) {
        var user = auth.currentUser
        val userUID = getUserUID()
        if (userUID.isEmpty()) return

        val userInfoRef = db.collection("userDetail").document(userUID)

        userInfoRef.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "User info deleted successfully.")
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Firebase", "User account deleted successfully.")

                            FirebaseAuth.getInstance().signOut()

                            Toast.makeText(context, "Account has been deleted.", Toast.LENGTH_SHORT).show()

                            Log.d("Current User", user.toString())
                            authViewModel.signout()
                        } else {
                            Log.w("Firebase", "Error deleting user account", task.exception)
                        }
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

    data class UserDetails(
        val name: String,
        val email: String,
        val phone: String,
        val dob: String,
        val nric: String
    )
}
