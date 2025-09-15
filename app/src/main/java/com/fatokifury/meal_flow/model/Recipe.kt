package com.fatokifury.meal_flow.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Recipe(
    var id: String = "", // Made var to allow update after Firestore doc creation
    val title: String = "",
    val imageUrl: String? = null,
    val description: String = "", // Optional: Add if you want a detailed description
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdBy: String? = null, // To store the UID of the user who created it
    @ServerTimestamp
    val createdAt: Date? = null // For sorting or tracking when it was added
)
