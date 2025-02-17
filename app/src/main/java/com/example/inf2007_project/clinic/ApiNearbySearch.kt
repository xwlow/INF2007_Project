package com.example.inf2007_project.clinic

import NearbySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiNearbySearch {
    @GET("place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        // LATER CHANGE TO KEYWORD
        @Query("keyword") keyword: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        //@Query("type") type: String,
        @Query("key") apiKey: String
    ): NearbySearchResponse
}