package com.fatokifury.meal_flow.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.NavigationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

data class AddRecipeUiState(
    val recipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val currentIngredient: String = "",
    val steps: List<String> = emptyList(),
    val currentStep: String = "",
    val selectedImageUri: Uri? = null,
    val tags: List<String> = emptyList(),
    val currentTag: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val auth: FirebaseAuth,
    private val navigationService: NavigationService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    private val recipeId: String? = savedStateHandle["recipeId"]

    init {
        if (recipeId != null && recipeId != "null") {
            loadRecipeForEditing(recipeId)
        }
    }

    private fun loadRecipeForEditing(id: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = recipeRepository.getRecipeById(id)
            result.onSuccess { recipe ->
                _uiState.update {
                    it.copy(
                        recipeId = recipe.id,
                        title = recipe.title,
                        description = recipe.description,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        tags = recipe.tags,
                        selectedImageUri = recipe.imageUrl?.toUri(),
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    // --- UI Event Handlers ---

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onCurrentImageUrlChange(newUri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = newUri) }
    }

    fun onCurrentIngredientChange(newIngredient: String) {
        _uiState.update { it.copy(currentIngredient = newIngredient) }
    }

    fun addIngredient() {
        val currentIngredient = _uiState.value.currentIngredient.trim()
        if (currentIngredient.isNotBlank()) {
            val updatedIngredients = _uiState.value.ingredients + currentIngredient
            _uiState.update { it.copy(ingredients = updatedIngredients, currentIngredient = "") }
        }
    }

    fun removeIngredient(ingredient: String) {
        val updatedIngredients = _uiState.value.ingredients - ingredient
        _uiState.update { it.copy(ingredients = updatedIngredients) }
    }

    fun onCurrentStepChange(newStep: String) {
        _uiState.update { it.copy(currentStep = newStep) }
    }

    fun addStep() {
        val currentStep = _uiState.value.currentStep.trim()
        if (currentStep.isNotBlank()) {
            val updatedSteps = _uiState.value.steps + currentStep
            _uiState.update { it.copy(steps = updatedSteps, currentStep = "") }
        }
    }

    fun removeStep(step: String) {
        val updatedSteps = _uiState.value.steps - step
        _uiState.update { it.copy(steps = updatedSteps) }
    }

    fun onCurrentTagChange(newTag: String) {
        _uiState.update { it.copy(currentTag = newTag) }
    }

    fun addTag() {
        val currentTag = _uiState.value.currentTag.trim()
        if (currentTag.isNotBlank()) {
            val updatedTags = _uiState.value.tags + currentTag
            _uiState.update { it.copy(tags = updatedTags, currentTag = "") }
        }
    }

    fun removeTag(tag: String) {
        val updatedTags = _uiState.value.tags - tag
        _uiState.update { it.copy(tags = updatedTags) }
    }

    fun saveRecipe() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title cannot be empty.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val recipeToSave = Recipe(
            id = currentState.recipeId ?: "",
            title = currentState.title,
            description = currentState.description,
            ingredients = currentState.ingredients + (if(currentState.currentIngredient.isNotBlank()) listOf(currentState.currentIngredient.trim()) else emptyList()),
            steps = currentState.steps + (if(currentState.currentStep.isNotBlank()) listOf(currentState.currentStep.trim()) else emptyList()),
            tags = currentState.tags + (if(currentState.currentTag.isNotBlank()) listOf(currentState.currentTag.trim()) else emptyList()),
            imageUrl = currentState.selectedImageUri.toString(),
            createdBy = auth.currentUser?.uid ?: ""
        )

        viewModelScope.launch {
            recipeRepository.saveRecipe(recipeToSave)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun onSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) } // Reset state
        navigationService.goBack()
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
