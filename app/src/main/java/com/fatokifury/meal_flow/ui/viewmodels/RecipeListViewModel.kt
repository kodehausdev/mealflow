package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.model.Recipe // Assuming Recipe has an 'id' field
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import for await()
import javax.inject.Inject

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true, // Default to true for initial load
    val errorMessage: String? = null,
    val recipeJustDeleted: Recipe? = null // For Snackbar/Undo later
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private var recipesListener: ListenerRegistration? = null

    init {
        Log.d("RecipeListVM", "viewModel initialized. Attaching Listener.")
        attachRecipesListener()
        // Log.d("RecipeListVM", "Previous listener removed if any.") // Already logged in attachRecipesListener
    }


    private fun attachRecipesListener() {
        Log.d("RecipeListVM", "attachRecipesListener called.")
        recipesListener?.remove() // Remove previous listener first
        Log.d("RecipeListVM", "Previous listener removed if any.")

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("RecipeListVM", "User is null. Cannot attach listener.")
            _uiState.value = RecipeListUiState(recipes = emptyList(), isLoading = false, errorMessage = "User not logged in. Cannot load recipes.")
            return
        }
        Log.d("RecipeListVM", "User: ${currentUser.uid}. Attaching snapshot listener.")

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, recipeJustDeleted = null) // Reset deleted recipe

        recipesListener = firestore.collection("recipes")
            .whereEqualTo("createdBy", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    val exceptionType = e.javaClass.name
                    val exceptionMessage = e.message ?: "No message available"
                    Log.e("RecipeListVM", "Error details - Type: $exceptionType, Message: $exceptionMessage", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading recipes: $exceptionMessage"
                    )
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val recipeList = snapshots.toObjects(Recipe::class.java)
                    Log.d("RecipeListVM", "Snapshot received. Recipe count: ${recipeList.size}")
                    _uiState.value = _uiState.value.copy(recipes = recipeList, isLoading = false, errorMessage = null)
                } else {
                    Log.w("RecipeListVM", "Snapshot was null, no error. Treating as no data.")
                    _uiState.value = _uiState.value.copy(
                        recipes = emptyList(),
                        isLoading = false,
                        errorMessage = "No recipes found."
                    )
                }
            }
        Log.d("RecipeListVM", "Snapshot listener attached.")
    }

    fun deleteRecipe(recipe: Recipe) {
        if (recipe.id.isBlank()) {
            Log.e("RecipeListVM", "Cannot delete recipe with blank ID.")
            _uiState.value = _uiState.value.copy(errorMessage = "Error: Recipe ID is missing.")
            return
        }
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("RecipeListVM", "User is null. Cannot delete recipe.")
            _uiState.value = _uiState.value.copy(errorMessage = "User not logged in.")
            return
        }

        Log.d("RecipeListVM", "Attempting to delete recipe with ID: ${recipe.id}")
        viewModelScope.launch {
            try {
                // Important: Verify ownership before deleting if not already handled by Firestore rules
                // For now, we assume the listener only fetches user's own recipes.
                firestore.collection("recipes").document(recipe.id).delete().await()
                Log.i("RecipeListVM", "Recipe deleted successfully from Firestore: ${recipe.id}")
                // The listener will automatically update the UI by removing the recipe.
                // For Snackbar/Undo, we can set recipeJustDeleted
                 _uiState.value = _uiState.value.copy(recipeJustDeleted = recipe, errorMessage = "${recipe.title} deleted")
            } catch (e: Exception) {
                Log.e("RecipeListVM", "Error deleting recipe: ${recipe.id}", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete ${recipe.title}: ${e.message}")
            }
        }
    }

    // Call this to clear the recipeJustDeleted, e.g., after Snackbar dismisses
    fun clearRecipeJustDeleted() {
        _uiState.value = _uiState.value.copy(recipeJustDeleted = null, errorMessage = null) // Clear related error message too
    }


    fun refreshRecipes() {
        Log.d("RecipeListVM", "refreshRecipes() called explicitly.")
        attachRecipesListener()
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, recipeJustDeleted = null) // Also clear recipeJustDeleted if error is dismissed
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("RecipeListVM", "onCleared called. Removing listener.")
        recipesListener?.remove()
    }
}
