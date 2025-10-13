package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Log
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val navigationService: NavigationService,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recipeRepository.getAllRecipes()
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .catch { exception ->
                    Log.e("RecipeListVM", "Error collecting recipes", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${exception.message}") }
                }
                .collect { recipeList ->
                    Log.d("RecipeListVM", "Snapshot received. Recipe count: ${recipeList.size}")
                    _uiState.update { it.copy(recipes = recipeList, isLoading = false) }
                }
        }
    }

    // --- Navigation Events --- //
    fun onRecipeSelected(recipeId: String) {
        // Corrected from navigateTo to just navigate if that's the name in your service
        navigationService.navigate(Screen.RecipeDetail.createRoute(recipeId))
    }

    fun onAddRecipeClicked() {
        // FIX: The AddRecipe route now requires an ID, even if it's for a new recipe.
        // We pass "new" or a similar keyword to signify a new recipe.
        navigationService.navigate(Screen.AddRecipe.createRoute("new"))
    }

    fun onImportRecipeClicked(url: String) {
        try {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            navigationService.navigate(Screen.ImportRecipe.createRoute(encodedUrl))
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Invalid URL format.") }
        }
    }

    fun onMealCalendarClicked() {
        navigationService.navigate(Screen.MealCalendar.route)
    }

    fun onLogoutClicked() {
        // FIX: Renamed navigateAndPopUpTo to navigateAndPopUp
        navigationService.navigateAndPopUp(Screen.Login.route, Screen.MealList.route)
    }

    // --- Data Events --- //
    fun deleteRecipe(recipe: Recipe) {
        if (recipe.id.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Error: Recipe ID is missing.") }
            return
        }
        viewModelScope.launch {
            // Your RecipeRepository's deleteRecipe probably doesn't return a Result anymore
            // since it throws an exception on failure. We'll wrap it in a try-catch.
            try {
                recipeRepository.deleteRecipe(recipe.id)
                // FIX: Corrected the typo here
                Log.i("RecipeListVM", "Recipe delete request sent for: ${recipe.id}")
                // The realtime listener will update the list, but we can show a temporary message.
                _uiState.update { it.copy(errorMessage = "${recipe.title} deleted") }
            } catch (e: Exception) {
                Log.e("RecipeListVM", "Error deleting recipe: ${recipe.id}", e)
                _uiState.update { ui -> ui.copy(errorMessage = "Failed to delete ${recipe.title}: ${e.message}") }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
