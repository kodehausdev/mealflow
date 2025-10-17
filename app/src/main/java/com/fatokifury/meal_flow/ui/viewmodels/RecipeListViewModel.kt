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

data class UserMessage(val id: Long, val message: String, val recipe: Recipe? = null)

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: UserMessage? = null,
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
                .onStart { _uiState.update { it.copy(isLoading = true, userMessage = null) } }
                .catch { exception ->
                    Log.e("RecipeListVM", "Error collecting recipes", exception)
                    val message = UserMessage(id = System.nanoTime(), message = "Error: ${exception.message}")
                    _uiState.update { it.copy(isLoading = false, userMessage = message) }
                }
                .collect { recipeList ->
                    Log.d("RecipeListVM", "Snapshot received. Recipe count: ${recipeList.size}")
                    _uiState.update { it.copy(recipes = recipeList, isLoading = false) }
                }

        }
    }

    // --- Navigation Events --- //
    fun onRecipeSelected(recipeId: String) {
        navigationService.navigate(Screen.RecipeDetail.createRoute(recipeId))
    }

    fun onAddRecipeClicked() {
        navigationService.navigate(Screen.AddRecipe.createRoute(null))
    }

    fun onImportRecipeClicked(url: String) {
        try {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            navigationService.navigate(Screen.ImportRecipe.createRoute(encodedUrl))
        } catch (e: Exception) {
            val message = UserMessage(id = System.nanoTime(), message = "Invalid URL format.")
            _uiState.update { it.copy(userMessage = message) }
        }
    }

    fun onMealCalendarClicked() {
        navigationService.navigate(Screen.MealCalendar.route)
    }

    fun onLogoutClicked() {
        // You should ideally handle the actual Firebase logout here
        navigationService.navigateAndPopUp(Screen.Login.route, Screen.MealList.route)
    }

    // --- Data Events --- //
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe.id).onSuccess {
                val message = UserMessage(
                    id = System.nanoTime(),
                    message = "'${recipe.title}' deleted.",
                    recipe = recipe
                )
                _uiState.update { it.copy(userMessage = message) }
            }.onFailure {
                val message = UserMessage(id = System.nanoTime(), message = "Failed to delete '${recipe.title}'.")
                _uiState.update { it.copy(userMessage = message) }
            }
        }
    }

    fun undoDelete(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(recipe).onFailure {
                val message = UserMessage(id = System.nanoTime(), message = "Failed to restore '${recipe.title}'.")
                _uiState.update { it.copy(userMessage = message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
