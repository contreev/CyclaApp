package com.example.cyclapp.model

data class RecyclingPoint(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val categories: List<String> = emptyList(),
    val averageRating: Double = 0.0,
    val openingHours: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val reviews: List<Review> = emptyList()
)

data class Review(
    val id: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val date: String = ""
)
