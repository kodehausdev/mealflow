package com.fatokifury.meal_flow.model

data class User(
    val uid: String = "",
    val familyId: String? = null,
    val email: String? = null // Optional: store email for display
)