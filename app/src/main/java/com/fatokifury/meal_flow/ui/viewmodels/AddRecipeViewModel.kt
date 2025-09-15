package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.SavedStateHandle // Added
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.builtins.ListSerializer // Added for JSON
import kotlinx.serialization.builtins.serializer // Added for JSON
import kotlinx.serialization.json.Json // Added for JSON
import java.net.URLDecoder // Added
import java.nio.charset.StandardCharsets // Added
import javax.inject.Inject

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "", // Added description field
    val ingredients: List<String> = emptyList(),
    val currentIngredient: String = "",
    val steps: List<String> = emptyList(),
    val currentStep: String = "",
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val currentTag: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle // Added SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    init {
        // Retrieve and decode arguments from SavedStateHandle
        val initialTitle = savedStateHandle.get<String>("title")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
        val initialImageUrl = savedStateHandle.get<String>("imageUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
        val initialDescription = savedStateHandle.get<String>("description")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

        val json = Json { ignoreUnknownKeys = true; isLenient = true } // Configure Json parser

        val initialIngredients = savedStateHandle.get<String>("ingredientsJson")?.let {
            try {
                json.decodeFromString(ListSerializer(String.serializer()), URLDecoder.decode(it, StandardCharsets.UTF_8.toString()))
            } catch (e: Exception) { emptyList<String>() /* Handle parsing error, default to empty */ }
        } ?: emptyList()

        val initialSteps = savedStateHandle.get<String>("stepsJson")?.let {
            try {
                json.decodeFromString(ListSerializer(String.serializer()), URLDecoder.decode(it, StandardCharsets.UTF_8.toString()))
            } catch (e: Exception) { emptyList<String>() }
        } ?: emptyList()

        val initialTags = savedStateHandle.get<String>("tagsJson")?.let {
            try {
                json.decodeFromString(ListSerializer(String.serializer()), URLDecoder.decode(it, StandardCharsets.UTF_8.toString()))
            } catch (e: Exception) { emptyList<String>() }
        } ?: emptyList()

        _uiState.value = AddRecipeUiState(
            title = initialTitle,
            imageUrl = initialImageUrl,
            description = initialDescription,
            ingredients = initialIngredients,
            steps = initialSteps,
            tags = initialTags
        )
    }


    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    // Added onDescriptionChange
    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onCurrentIngredientChange(newIngredient: String) {
        _uiState.value = _uiState.value.copy(currentIngredient = newIngredient)
    }

    fun addIngredient() {
        val currentIngredient = _uiState.value.currentIngredient.trim()
        if (currentIngredient.isNotBlank()) {
            val updatedIngredients = _uiState.value.ingredients + currentIngredient
            _uiState.value = _uiState.value.copy(
                ingredients = updatedIngredients,
                currentIngredient = ""
            )
        }
    }

    fun removeIngredient(ingredient: String) {
        val updatedIngredients = _uiState.value.ingredients - ingredient
        _uiState.value = _uiState.value.copy(ingredients = updatedIngredients)
    }


    fun onCurrentStepChange(newStep: String) {
        _uiState.value = _uiState.value.copy(currentStep = newStep)
    }

    fun addStep() {
        val currentStep = _uiState.value.currentStep.trim()
        if (currentStep.isNotBlank()) {
            val updatedSteps = _uiState.value.steps + currentStep
            _uiState.value = _uiState.value.copy(
                steps = updatedSteps,
                currentStep = ""
            )
        }
    }

    fun removeStep(step: String) {
        val updatedSteps = _uiState.value.steps - step
        _uiState.value = _uiState.value.copy(steps = updatedSteps)
    }

    fun onCurrentTagChange(newTag: String) {
        _uiState.value = _uiState.value.copy(currentTag = newTag)
    }

    fun addTag() {
        val currentTag = _uiState.value.currentTag.trim()
        if (currentTag.isNotBlank()) {
            val updatedTags = _uiState.value.tags + currentTag
            _uiState.value = _uiState.value.copy(
                tags = updatedTags,
                currentTag = ""
            )
        }
    }

    fun removeTag(tag: String) {
        val updatedTags = _uiState.value.tags - tag
        _uiState.value = _uiState.value.copy(tags = updatedTags)
    }


    fun onImageUrlChange(newUrl: String?) {
        _uiState.value = _uiState.value.copy(imageUrl = newUrl)
    }

    fun saveRecipe() {
        if (_uiState.value.title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title cannot be empty.")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "User not logged in.")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

        var finalIngredients = _uiState.value.ingredients
        if (_uiState.value.currentIngredient.isNotBlank()) {
            finalIngredients = finalIngredients + _uiState.value.currentIngredient.trim()
        }

        var finalSteps = _uiState.value.steps
        if (_uiState.value.currentStep.isNotBlank()) {
            finalSteps = finalSteps + _uiState.value.currentStep.trim()
        }

        var finalTags = _uiState.value.tags
        if (_uiState.value.currentTag.isNotBlank()) {
            finalTags = finalTags + _uiState.value.currentTag.trim()
        }

        viewModelScope.launch {
            try {
                val newRecipeRef = firestore.collection("recipes").document()

                val recipeToSave = Recipe(
                    id = newRecipeRef.id,
                    title = _uiState.value.title,
                    description = _uiState.value.description, // Save description
                    ingredients = finalIngredients,
                    steps = finalSteps,
                    imageUrl = _uiState.value.imageUrl,
                    tags = finalTags,
                    createdBy = currentUser.uid
                )

                newRecipeRef.set(recipeToSave).await()
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    currentIngredient = "",
                    currentStep = "",
                    currentTag = ""
                )
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Failed to save recipe.")
            }
        }
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(saveSuccess = false, errorMessage = null, isSaving = false) // also reset isSaving
    }
}

