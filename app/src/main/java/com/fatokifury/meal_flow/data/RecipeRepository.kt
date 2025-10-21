package com.fatokifury.meal_flow.data

import android.net.Uri

import com.fatokifury.meal_flow.model.Recipe
import kotlinx.coroutines.flow.Flow // <-- Make sure this import is added

// Notice this is now an 'interface', not a 'class'. It has no constructor.
interface RecipeRepository {
    fun getNewRecipeId(): String

    suspend fun saveRecipe(recipe: Recipe): Result<String>

    suspend fun uploadImage(uri: Uri, recipeId: String, userId: String): Result<String>

    suspend fun getRecipeById(recipeId: String): Result<Recipe>

    // --- ADD THESE NEW FUNCTIONS ---
    fun getAllRecipes(): Flow<List<Recipe>> // For real-time updates from Firestore
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
}

