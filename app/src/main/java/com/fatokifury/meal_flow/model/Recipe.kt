package com.fatokifury.meal_flow.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Recipe(
    var id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val description: String = "",
    val servings: Int = 1, // Base serving size
    val ingredients: List<Ingredient> = emptyList(), // Use the new data class
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdBy: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val sourceUrl: String = ""
)
