package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Ingredient
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
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val displayedServings: Int = 1, // Holds the user-selected serving size
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

    fun loadRecipeDetails() {
        if (recipeId == null || recipeId == "new") {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Recipe ID not found.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result: Result<Recipe> = recipeRepository.getRecipeById(recipeId)

            result.onSuccess { recipe ->
                _uiState.update {
                    it.copy(
                        recipe = recipe,
                        isLoading = false,
                        displayedServings = recipe.servings
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun onServingsChanged(newServings: Int) {
        if (newServings > 0) {
            _uiState.update { it.copy(displayedServings = newServings) }
        }
    }

    fun getAdjustedIngredientQuantity(ingredient: Ingredient): Double {
        val recipe = _uiState.value.recipe ?: return ingredient.quantity
        if (recipe.servings == 0) return ingredient.quantity
        return (ingredient.quantity / recipe.servings) * _uiState.value.displayedServings
    }

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