package com.fatokifury.meal_flow.ui.viewmodels

import android.app.Application
import android.content.Intent
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
import com.fatokifury.meal_flow.model.Ingredient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class AddRecipeUiState(
    val recipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val servings: String = "1",
    val ingredients: List<Ingredient> = emptyList(),
    val currentIngredientName: String = "",
    val currentIngredientQuantity: String = "",
    val currentIngredientUnit: String = "",
    val steps: List<String> = emptyList(),
    val currentStep: String = "",
    val selectedImageUri: Uri? = null,
    val existingImageUrl: String? = null,
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
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

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
                        servings = recipe.servings.toString(),
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        tags = recipe.tags,
                        existingImageUrl = recipe.imageUrl,
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

    fun onServingsChange(newServings: String) {
        _uiState.update { it.copy(servings = newServings) }
    }

    fun onCurrentImageUrlChange(newUri: Uri?) {
        newUri?.let { uri ->
            try {
                val contentResolver = application.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        _uiState.update { it.copy(selectedImageUri = newUri) }
    }

    // --- Ingredient Handlers ---

    fun onCurrentIngredientNameChange(newName: String) {
        _uiState.update { it.copy(currentIngredientName = newName) }
    }

    fun onCurrentIngredientQuantityChange(newQuantity: String) {
        _uiState.update { it.copy(currentIngredientQuantity = newQuantity) }
    }

    fun onCurrentIngredientUnitChange(newUnit: String) {
        _uiState.update { it.copy(currentIngredientUnit = newUnit) }
    }

    fun addIngredient() {
        val state = _uiState.value
        val name = state.currentIngredientName.trim()
        val quantity = state.currentIngredientQuantity.trim().toDoubleOrNull()
        val unit = state.currentIngredientUnit.trim()

        if (name.isNotBlank() && quantity != null && quantity > 0) {
            val newIngredient = Ingredient(name = name, quantity = quantity, unit = unit)
            val updatedIngredients = state.ingredients + newIngredient
            _uiState.update {
                it.copy(
                    ingredients = updatedIngredients,
                    currentIngredientName = "",
                    currentIngredientQuantity = "",
                    currentIngredientUnit = ""
                )
            }
        } else {
            viewModelScope.launch {
                _toastMessage.emit("Invalid ingredient details.")
            }
        }
    }

    fun removeIngredient(ingredient: Ingredient) {
        val updatedIngredients = _uiState.value.ingredients - ingredient
        _uiState.update { it.copy(ingredients = updatedIngredients) }
    }

    // --- Step Handlers ---

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

    // --- Tag Handlers ---

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

    // --- Save Logic ---
    fun saveRecipe() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.title.isBlank()) {
                _toastMessage.emit("Recipe title cannot be empty.")
                return@launch
            }

            val servings = currentState.servings.toIntOrNull()
            if (servings == null || servings <= 0) {
                _toastMessage.emit("Please enter a valid serving size.")
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }

            try {
                val recipeIdToUse = currentState.recipeId ?: recipeRepository.getNewRecipeId()

                // FIXED: Store the URI directly as a string (no Firebase upload)
                // If a new image was selected, use selectedImageUri
                // Otherwise, keep the existingImageUrl
                val imageUrlToSave = currentState.selectedImageUri?.toString()
                    ?: currentState.existingImageUrl

                val recipeToSave = Recipe(
                    id = recipeIdToUse,
                    title = currentState.title,
                    description = currentState.description,
                    servings = servings,
                    ingredients = currentState.ingredients,
                    steps = currentState.steps,
                    tags = currentState.tags,
                    imageUrl = imageUrlToSave, // Save URI as string
                    createdBy = auth.currentUser?.uid ?: ""
                )

                val saveResult = recipeRepository.saveRecipe(recipeToSave)

                saveResult.fold(
                    onSuccess = { savedRecipeId ->
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true, recipeId = savedRecipeId) }
                        _toastMessage.emit("Recipe saved successfully!")
                        navigationService.goBack()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSaving = false) }
                        _toastMessage.emit("Error saving recipe: ${e.message}")
                    }
                )

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _toastMessage.emit("An unexpected error occurred: ${e.message}")
            }
        }
    }
}