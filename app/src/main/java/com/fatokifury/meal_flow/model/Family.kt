package com.fatokifury.meal_flow.model

data class Family(
    val id: String = "",
    val members: List<String> = emptyList(),
    val name: String? = null
)