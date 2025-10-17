package com.fatokifury.meal_flow.data

import android.net.Uri // Import Uri
import com.fatokifury.meal_flow.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage // Import FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID // Import UUID for unique filenames
import javax.inject.Inject
import javax.inject.Singleton

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun saveRecipe(recipe: Recipe): Result<String>
    suspend fun getRecipeById(id: String): Result<Recipe>

    // 1. Add the new function to the interface
    suspend fun uploadImage(imageUri: Uri): Result<String>
}

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    // 2. Inject FirebaseStorage
    private val storage: FirebaseStorage
) : RecipeRepository {

    // 3. ADD THE NEW UPLOADIMAGE FUNCTION IMPLEMENTATION
    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated."))
            // Create a unique filename (e.g., images/userId/randomUUID.jpg)
            val fileName = "images/${currentUser.uid}/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            // Upload the file
            storageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllRecipes(): Flow<List<Recipe>> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return emptyFlow()
        }

        return firestore.collection("recipes")
            .whereEqualTo("createdBy", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Recipe>()
            }
    }

    override suspend fun getRecipeById(id: String): Result<Recipe> {
        return try {
            val document = firestore.collection("recipes").document(id).get().await()
            val recipe = document.toObject(Recipe::class.java)
            if (recipe != null) {
                Result.success(recipe.copy(id = document.id))
            } else {
                Result.failure(Exception("Recipe not found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated."))
            val recipeWithUser = recipe.copy(createdBy = currentUser.uid)

            if (recipeWithUser.id.isBlank()) {
                val newDocRef = firestore.collection("recipes").document()
                val finalRecipe = recipeWithUser.copy(id = newDocRef.id)
                newDocRef.set(finalRecipe).await()
                Result.success(finalRecipe.id)
            } else {
                firestore.collection("recipes").document(recipeWithUser.id).set(recipeWithUser).await()
                Result.success(recipeWithUser.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            if (recipeId.isBlank()) {
                throw IllegalArgumentException("Recipe ID cannot be blank.")
            }
            firestore.collection("recipes").document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
