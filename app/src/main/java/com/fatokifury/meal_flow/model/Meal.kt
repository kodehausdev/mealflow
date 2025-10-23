package com.fatokifury.meal_flow.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK //
}

data class Meal(
    val id: String = "",
    val name: String = "",
    val type: MealType = MealType.DINNER,
    @ServerTimestamp val date: Date? = null,
    val addedBy: String = ""
)