package com.example.inf2007_project.uam

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel(){

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    //val auth = FirebaseAuth.getInstance()
    //val context = LocalContext.current

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState
    private val _userActionState = MutableLiveData<String>()
    val userActionState: LiveData<String> get() = _userActionState

    init{
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }
        else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email:String, password:String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cannot be empty!")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signup(email:String, password:String, onResult: (String?) -> Unit){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cannot be empty!")
            onResult(null)
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    val uid = auth.currentUser?.uid
                    _authState.value = AuthState.Unauthenticated
                    onResult(uid)
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                    onResult(null)
                }
            }


    }

//    fun addUserDetails(name: String, nric: String, email: String) {
//        if (name.isBlank() || nric.isBlank() || email.isBlank()) {
//            _userActionState.value = "All fields are required!"
//            return
//        }
//
//        val data = mapOf(
//            "name" to name,
//            "nric" to nric,
//            "email" to email
//        )
//
//        val documentId = email.lowercase()
//
//        db.collection("userDetail").document(documentId).set(data)
//            .addOnSuccessListener {
//                _userActionState.value = "Account created successfully!"
//                navController.navigate("Login")
//            }
//            .addOnFailureListener { e ->
//                _userActionState.value = "Error adding account: ${e.message}"
//            }
//    }


    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated: AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}

