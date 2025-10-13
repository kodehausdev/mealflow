package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
    private val navigationService: NavigationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val recipeId: String? = savedStateHandle["recipeId"]

    init {
        loadRecipeDetails()
    }

    private fun loadRecipeDetails() {
        if (recipeId == null || recipeId == "new") {_uiState.update { it.copy(isLoading = false, errorMessage = "Recipe ID not found.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // 1. Call the function and store the 'Result<Recipe>' object.
            val result: Result<Recipe> = recipeRepository.getRecipeById(recipeId)

            // 2. Use the built-in 'onSuccess' and 'onFailure' handlers to unwrap the Result.
            result.onSuccess { recipe ->
                // This code only runs if the result was a success.
                // The 'recipe' variable here is the actual Recipe object.
                _uiState.update { it.copy(recipe = recipe, isLoading = false) }

            }.onFailure { exception ->
                // This code only runs if the result was a failure.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }


        // These functions are now correctly defined at the class level.
        fun onNavigateBack() {
            navigationService.goBack()
        }

        fun onEditRecipeClicked() {
            if (recipeId != null) {
                navigationService.navigate(Screen.AddRecipe.createRoute(recipeId))
            }
        }

        fun clearErrorMessage() {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }
