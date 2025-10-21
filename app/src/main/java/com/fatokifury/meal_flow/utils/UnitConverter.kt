package com.fatokifury.meal_flow.utils

object UnitConverter {
    private val gramsPerCup = mapOf(
        "all-purpose flour" to 120.0,
        "granulated sugar" to 200.0,
        "brown sugar" to 200.0,
        "butter" to 227.0,
        "water" to 236.0,
        "milk" to 240.0
    )

    /**
     * Converts a quantity from one unit to another, if possible.
     * @param quantity The amount to convert.
     * @param fromUnit The starting unit (e.g., "cup").
     * @param toUnit The target unit (e.g., "g").
     * @param ingredientName The name of the ingredient, for density-based conversions.
     * @return A Pair of the new quantity and unit, or null if conversion is not possible.
     */
    fun convert(quantity: Double, fromUnit: String, toUnit: String, ingredientName: String): Pair<Double, String>? {
        val from = fromUnit.lowercase().trim()
        val to = toUnit.lowercase().trim()
        val name = ingredientName.lowercase().trim()

        if (from == "cup" && to == "g") {
            val conversionFactor = gramsPerCup[name] ?: return null // Unknown ingredient
            val convertedQuantity = quantity * conversionFactor
            return Pair(convertedQuantity, "g")
        }
        // Future logic for g -> cup, or other conversions, can be added here.
        return null
    }
}
