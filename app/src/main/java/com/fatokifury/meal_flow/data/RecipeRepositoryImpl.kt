package com.fatokifury.meal_flow.data

import android.net.Uri
import com.fatokifury.meal_flow.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow // <-- Add this import
import kotlinx.coroutines.flow.map // <-- Add this import
import kotlinx.coroutines.flow.emptyFlow




class RecipeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : RecipeRepository {

// In RecipeRepositoryImpl.kt

    override fun getAllRecipes(): Flow<List<Recipe>> {
        // 1. Get the current user ID safely.
        // The ?.uid returns null if currentUser is null.
        // The .takeIf { it.isNotBlank() } returns null if the uid is blank.
        val userId = auth.currentUser?.uid.takeIf { !it.isNullOrBlank() }

        // 2. If userId is null or blank at this point, immediately return an empty flow.
        // This is our crash protection.
        if (userId == null) {
            return emptyFlow()
        }

        // 3. The rest of the code now runs only if we have a valid userId.
        return firestore.collection("recipes")
            .whereEqualTo("createdBy", userId) // Use the safe userId variable
            .snapshots()
            .map { snapshot ->
                try {
                    // This try-catch is for data conversion errors, which is also good practice.
                    snapshot.toObjects<Recipe>()
                } catch (e: Exception) {
                    // Log the error in a real app, for now, return empty list to prevent crash
                    // Log.e("Firestore", "Error converting recipes", e)
                    emptyList()
                }
            }
    }




    override suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            firestore.collection("recipes").document(recipeId).delete().await()
            Result.success(Unit) // Return success
        } catch (e: Exception) {
            Result.failure(e) // Return failure
        }
    }



    // We add the 'override' keyword because we are fulfilling the interface contract.
    override fun getNewRecipeId(): String = firestore.collection("recipes").document().id

    override suspend fun saveRecipe(recipe: Recipe): Result<String> {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            try {
                val documentReference = if (recipe.id.isNotBlank()) {
                    firestore.collection("recipes").document(recipe.id)
                } else {
                    firestore.collection("recipes").document()
                }

                val recipeToSave = recipe.copy(id = documentReference.id, createdBy = currentUser.uid)
                documentReference.set(recipeToSave).await()
                Result.success(documentReference.id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }

    override suspend fun uploadImage(uri: Uri, recipeId: String, userId: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("recipe_images/$userId/$recipeId/${uri.lastPathSegment}")
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecipeById(recipeId: String): Result<Recipe> {
        return try {
            val document = firestore.collection("recipes").document(recipeId).get().await()
            val recipe = document.toObject(Recipe::class.java)
            if (recipe != null) {
                Result.success(recipe)
            } else {
                Result.failure(Exception("Recipe not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
