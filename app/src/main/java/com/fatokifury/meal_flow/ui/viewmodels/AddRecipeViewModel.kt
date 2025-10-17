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
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class AddRecipeUiState(
    val recipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val currentIngredient: String = "",
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

    // Add these two lines
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
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        tags = recipe.tags,
                        selectedImageUri = recipe.imageUrl?.toUri(),
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

    fun onCurrentImageUrlChange(newUri: Uri?) {    // If a new image is selected (URI is not null)
        newUri?.let { uri ->
            try {
                // Get the ContentResolver from the application context
                val contentResolver = application.contentResolver

                // Define the permission flags we need
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

                // This is the crucial line:
                // It tells the system "My app wants to keep access to this URI"
                contentResolver.takePersistableUriPermission(uri, takeFlags)

            } catch (e: SecurityException) {
                // This can happen if the URI is from a provider that doesn't support
                // persistable permissions. Log it for debugging.
                e.printStackTrace()
                // Optionally, you could emit a toast message here to inform the user.
            }
        }
        // Finally, update the UI state with the URI
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


// In AddRecipeViewModel.kt

    fun saveRecipe() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.title.isBlank()) {
                _toastMessage.emit("Recipe title cannot be empty.")
                return@launch
            }

            try {
                // --- SIMPLIFIED LOGIC ---
                // We will directly save the URI as a string.
                // If a new image was picked, selectedImageUri will have a value.
                // If editing without changing the image, existingImageUrl will be used.
                val imageUrlToSave =
                    currentState.selectedImageUri?.toString() ?: currentState.existingImageUrl

                // Create the Recipe object with the URI string.
                val recipeToSave = Recipe(
                    id = currentState.recipeId ?: "",
                    title = currentState.title,
                    description = currentState.description,
                    ingredients = currentState.ingredients,
                    steps = currentState.steps,
                    tags = currentState.tags,
                    imageUrl = imageUrlToSave, // Use the URI string directly
                    createdBy = auth.currentUser?.uid ?: ""
                )

                // Save the recipe object to Firestore.
                val saveResult = recipeRepository.saveRecipe(recipeToSave)

                saveResult.fold(
                    onSuccess = { savedRecipeId ->
                        _uiState.update { it.copy(recipeId = savedRecipeId) }
                        _toastMessage.emit("Recipe saved successfully!")
                        navigationService.goBack()
                    },
                    onFailure = {
                        _toastMessage.emit("Error saving recipe: ${it.message}")
                    }
                )

            } catch (e: Exception) {
                _toastMessage.emit("An unexpected error occurred: ${e.message}")
            }
        }
    }
}

