package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    // Assuming your navigation argument is named "recipeId"
    private val recipeId: String? = savedStateHandle["recipeId"]

    init {
        loadRecipeDetails()
    }

    private fun loadRecipeDetails() {
        if (recipeId == null) {
            _uiState.value = RecipeDetailUiState(isLoading = false, errorMessage = "Recipe ID not found.")
            return
        }

        _uiState.value = RecipeDetailUiState(isLoading = true, errorMessage = null) // Start loading

        viewModelScope.launch {
            try {
                val documentSnapshot = firestore.collection("recipes")
                    .document(recipeId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val recipe = documentSnapshot.toObject(Recipe::class.java)
                    _uiState.value = RecipeDetailUiState(recipe = recipe, isLoading = false)
                } else {
                    _uiState.value = RecipeDetailUiState(isLoading = false, errorMessage = "Recipe not found.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeDetailUiState(
                    isLoading = false,
                    errorMessage = "Failed to load recipe details: ${e.message}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
