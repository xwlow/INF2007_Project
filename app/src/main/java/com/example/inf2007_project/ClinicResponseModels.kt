package com.example.inf2007_project

data class ApiResponse(
    val result: Result
)

data class Result(
    val records: List<ClinicResponseModels>
)

data class ClinicResponseModels(
    val name: String,
    val created_at: String,
    val updated_at: String,
    val hci_code: String,
    val coordinates: String,
    val category: String,
    val block: String,
    val street_name: String,
    val building_name: String?,
    val floor_number: String,
    val unit_number: String,
    val postal_code: String
)

