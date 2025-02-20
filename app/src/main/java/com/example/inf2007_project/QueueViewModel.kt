package com.example.inf2007_project

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QueueViewModel : ViewModel() {
    // Connect to DB
    private val db = FirebaseFirestore.getInstance()

    // State to hold queue information
    private val _queueCount = mutableStateOf<Int?>(0)
    val queueCount: Int? get() = _queueCount.value

    private val _hasQueue = mutableStateOf<Boolean?>(null)
    val hasQueue: Boolean? get() = _hasQueue.value

    private val _isAddingQueue = mutableStateOf(false)
    val isAddingQueue: Boolean get() = _isAddingQueue.value

    private val _addQueueError = mutableStateOf<String?>(null)
    val addQueueError: String? get() = _addQueueError.value

    private val _isCheckingQueue = mutableStateOf(false)
    val isCheckingQueue: Boolean get() = _isCheckingQueue.value

    private val _checkQueueError = mutableStateOf<String?>(null)
    val checkQueueError: String? get() = _checkQueueError.value

    // Check if Clinic has queue
    fun checkQueue(clinicID: String) {
        viewModelScope.launch {
            try {
                val queueDocument = db.collection("queues")
                    .document(clinicID)
                    .get()
                    .await()

                if (queueDocument.exists()) {
                    val users = queueDocument.get("users") as? List<*>
                    val count = users?.size ?: 0
                    if (_queueCount.value != count) { // Only update if the count changes
                        _queueCount.value = count
                    }
                    _hasQueue.value = !users.isNullOrEmpty()
                    Log.d("QueueState", "Queue Count: ${_queueCount.value}")
                } else {
                    Log.d("QueueState", "Doesn't Exist")
                    _hasQueue.value = false
                    _queueCount.value = 0
                }
            } catch (e: Exception) {
                Log.d("QueueState", "catch't Exist")
                _hasQueue.value = null
                _queueCount.value = 0 // Set to 0, not null
                _checkQueueError.value = e.message
            } finally {
                _isCheckingQueue.value = false
            }
        }
    }

    fun addQueue(clinicID: String, userID: String) {
        viewModelScope.launch {
            _isAddingQueue.value = true
            _addQueueError.value = null

            val clinicDocRef = db.collection("queues").document(clinicID)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(clinicDocRef)

                if (!snapshot.exists()) {
                    // Create a new document for the clinic
                    transaction.set(clinicDocRef, hashMapOf("count" to 1, "users" to listOf(userID)))
                } else {
                    // Update the existing document
                    val currentCount = snapshot.get("count")?.toString()?.toIntOrNull() ?: 0
                    val users = snapshot.get("users") as? List<String> ?: emptyList()
                    transaction.update(clinicDocRef, "count", currentCount + 1)
                    transaction.update(clinicDocRef, "users", users + userID)
                }
            }.addOnSuccessListener {
                _isAddingQueue.value = false
                checkQueue(clinicID) // Refresh queue data
            }.addOnFailureListener { exception ->
                _isAddingQueue.value = false
                _addQueueError.value = exception.message
            }
        }
    }

    fun deleteUserFromQueue(clinicID: String, userID: String) {
        viewModelScope.launch {
            try {
                _isAddingQueue.value = true
                val clinicRef = db.collection("queues").document(clinicID)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(clinicRef)
                    val currentUsers = snapshot.get("users") as? MutableList<String>
                    val currentCount = snapshot.getLong("count")?.toInt() ?: 0

                    if (currentUsers != null && currentUsers.contains(userID)) {
                        currentUsers.remove(userID)
                        val updatedCount =
                            (currentCount - 1).coerceAtLeast(0) // Ensure count doesn't go below 0

                        // Update Firestore
                        transaction.update(clinicRef, "users", currentUsers)
                        transaction.update(clinicRef, "count", updatedCount)
                    }
                }
            } catch (e: Exception) {
                _addQueueError.value = e.message
            } finally {
                checkQueue(clinicID) // Refresh queue data
                _isAddingQueue.value = false
            }
        }
    }
}

