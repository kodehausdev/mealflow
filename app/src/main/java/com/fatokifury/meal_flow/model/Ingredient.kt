package com.fatokifury.meal_flow.model

data class Ingredient(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "" // e.g., "cup", "g", "tbsp"
)
