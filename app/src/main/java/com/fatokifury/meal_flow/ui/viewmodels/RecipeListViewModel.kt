package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State // <-- Add this import
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
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
    val isGridView: Boolean = false
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val navigationService: NavigationService,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    // --- State Management for Import Dialog --- //
    private val _importUrl = mutableStateOf(TextFieldValue(""))
    val importUrl: State<TextFieldValue> = _importUrl

    private val _showImportDialog = mutableStateOf(false)
    val showImportDialog: State<Boolean> = _showImportDialog
    // --- End of State Management --- //


    init {
        viewModelScope.launch {
            recipeRepository.getAllRecipes()
                .onStart { _uiState.update { it.copy(isLoading = true, userMessage = null) } }
                .catch { exception ->
                    Log.e("RecipeListVM", "Error collecting recipes", exception)
                    val message =
                        UserMessage(id = System.nanoTime(), message = "Error: ${exception.message}")
                    _uiState.update { it.copy(isLoading = false, userMessage = message) }
                }
                .collect { recipeList ->
                    Log.d("RecipeListVM", "Snapshot received. Recipe count: ${recipeList.size}")
                    _uiState.update { it.copy(recipes = recipeList, isLoading = false) }
                }
        }
    }


    // --- Functions to Control Import Dialog --- //

    // 1. Called by the "Import Recipe" button in the EmptyState or FAB.
    // Its ONLY job is to show the dialog.
    fun onImportRecipeClicked() {
        _importUrl.value = TextFieldValue("") // Clear previous URL
        _showImportDialog.value = true
    }

    // 2. Called by the dialog to close itself.
    fun onImportDialogDismiss() {
        _showImportDialog.value = false
    }

    // 3. Called every time the user types in the dialog's text field.
    fun onImportUrlChange(newValue: TextFieldValue) {
        _importUrl.value = newValue
    }

    // 4. Called when the user clicks the "Import" button INSIDE the dialog.
    // This function does the actual navigation.
    fun onImportFromUrl(url: String) {
        _showImportDialog.value = false // Hide the dialog

        if (url.isBlank()) {
            val message = UserMessage(id = System.nanoTime(), message = "No URL provided.")
            _uiState.update { it.copy(userMessage = message) }
            return
        }

        try {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            navigationService.navigate(Screen.ImportRecipe.createRoute(encodedUrl))
        } catch (e: Exception) {
            val message = UserMessage(id = System.nanoTime(), message = "Invalid URL format.")
            _uiState.update { it.copy(userMessage = message) }
        }
    }

    // --- Other Navigation and Data Events --- //

    fun onRecipeSelected(recipeId: String) {
        navigationService.navigate(Screen.RecipeDetail.createRoute(recipeId))
    }

    fun onAddRecipeClicked() {
        navigationService.navigate(Screen.AddRecipe.createRoute(null))
    }

    fun onMealCalendarClicked() {
        navigationService.navigate(Screen.MealCalendar.route)
    }

    fun onLogoutClicked() {
        navigationService.navigateAndPopUp(Screen.Login.route, Screen.MealList.route)
    }

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
                val message = UserMessage(
                    id = System.nanoTime(),
                    message = "Failed to delete '${recipe.title}'."
                )
                _uiState.update { it.copy(userMessage = message) }
            }
        }
    }

    fun undoDelete(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.saveRecipe(recipe).onFailure {
                val message = UserMessage(
                    id = System.nanoTime(),
                    message = "Failed to restore '${recipe.title}'."
                )
                _uiState.update { it.copy(userMessage = message) }
            }
        }
    }
        fun onToggleView() {
            _uiState.update { currentState ->
                currentState.copy(isGridView = !currentState.isGridView)
            }
        }

        fun userMessageShown() {
            _uiState.update { it.copy(userMessage = null) }
        }

    }
