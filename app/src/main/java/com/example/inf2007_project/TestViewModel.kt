package com.example.inf2007_project

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestViewModel(): ViewModel(){
    fun saveData(
        testData: testData,
        context: Context
    ) = CoroutineScope(Dispatchers.IO).launch {

        val fireStoreRef = Firebase.firestore
            .collection("test")
            .document(testData.testField)

        try{
            fireStoreRef.set(testData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Successfully Added Data", Toast.LENGTH_SHORT).show()
                }

        } catch(e: Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun retrieveData(testField: String,
                     context: Context,
                     data : (testData) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
                         val fireStoreRef = Firebase.firestore
                             .collection("test")
                             .document(testField)

                        try {
                            fireStoreRef.get().addOnSuccessListener {
                                if(it.exists()) {
                                    val testData = it.toObject<testData>()!!
                                    data(testData)
                                } else {
                                    Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        catch (e: Exception){
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
    }
}

