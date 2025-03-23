package com.example.inf2007_project.clinicList


import com.example.inf2007_project.clinic.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterfaceClinic {
    @GET("datastore_search?resource_id=d_3cd840069e95b6a521aa5301a084b25a")
    fun getData(): Call<ApiResponse>
}