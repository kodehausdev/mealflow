package com.fatokifury.meal_flow.data

import android.net.Uri
import com.fatokifury.meal_flow.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.ktx.snapshots // <-- Add this import
import kotlinx.coroutines.flow.Flow // <-- Add this import
import kotlinx.coroutines.flow.map // <-- Add this import



class RecipeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : RecipeRepository {



    override fun getAllRecipes(): Flow<List<Recipe>> {
        val currentUser = auth.currentUser
        // Return an empty flow if the user is not logged in
        return firestore.collection("recipes")
            .whereEqualTo("createdBy", currentUser?.uid)
            .snapshots() // This returns a Flow of QuerySnapshot
            .map { snapshot -> snapshot.toObjects(Recipe::class.java) } // Convert to List<Recipe>
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
