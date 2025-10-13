package com.fatokifury.meal_flow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.NavigationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle // Make sure this import is present

data class ImportRecipeUiState(
    val isLoading: Boolean = false,
    val importUrl: String? = null,
    val errorMessage: String? = null,
    val newRecipeId: String? = null
)

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    private val navigationService: NavigationService,
    private val application: Application,
    private val recipeRepository: RecipeRepository,
    private val savedStateHandle: SavedStateHandle // <-- ADD THIS
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportRecipeUiState())
    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()

    fun startImport() {
        // 1. Get the encoded URL from the SavedStateHandle.
        val encodedUrl: String? = savedStateHandle["encodedUrl"]

        //2. NEW: Add a null check BEFORE the try-catch block.
        // This is the real source of the problem. If the URL is null, we stop here.
        if (encodedUrl == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No URL was provided.") }
            return
        }

        // 3. Decode the non-null URL.
        val url = try {
            URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            // This catch block is now only for truly malformed URLs, not null ones.
            _uiState.update { it.copy(isLoading = false, errorMessage = "The provided URL is invalid.") }
            return
        }

        // 4. The rest of your original logic remains the same and is now reachable.
        _uiState.update { it.copy(isLoading = true, importUrl = url, errorMessage = null, newRecipeId = null) }

        val requestQueue = Volley.newRequestQueue(application)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response -> parseHtmlAndSave(response) },
            { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to fetch URL: ${error.message}") }
            }
        )
        requestQueue.add(stringRequest)
    }



    fun onNavigateBack() {
        navigationService.goBack()
    }



    private fun parseHtmlAndSave(htmlContent: String) {    viewModelScope.launch {
        try {
            // 1. Create the recipe object from the HTML content.
            val recipe = parseJsonLd(htmlContent)

            // 2. The repository now handles the `createdBy` logic.
            //    We no longer need to add it here.
            val result = recipeRepository.saveRecipe(recipe)

            result.onSuccess {
                // 3. The 'recipe' object already contains the ID assigned by the repository.
                //    Use recipe.id instead of 'it'.
                _uiState.update { ui -> ui.copy(isLoading = false, newRecipeId = recipe.id) }

            }.onFailure { exception ->
                _uiState.update { ui -> ui.copy(isLoading = false, errorMessage = "Failed to save imported recipe: ${exception.message}") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to parse recipe data: ${e.message}") }
        }
    }
    }


    private fun parseJsonLd(htmlContent: String): Recipe {
        val scriptTagStart = "<script type=\"application/ld+json\">"
        val scriptTagEnd = "</script>"

        var startIndex = 0
        while (startIndex != -1) {
            startIndex = htmlContent.indexOf(scriptTagStart, startIndex)
            if (startIndex != -1) {
                val endIndex = htmlContent.indexOf(scriptTagEnd, startIndex)
                if (endIndex != -1) {
                    val jsonLdContent = htmlContent.substring(startIndex + scriptTagStart.length, endIndex).trim()
                    try {
                        val jsonObject = JSONObject(jsonLdContent)
                        val recipeObject = findRecipeObject(jsonObject)

                        if (recipeObject != null) {
                            val ingredients = recipeObject.optJSONArray("recipeIngredient")?.let { parseJsonArrayToStringList(it) } ?: emptyList()
                            val instructions = recipeObject.optJSONArray("recipeInstructions")?.let { parseInstructionArray(it) } ?: emptyList()
                            val imageUrl = parseImageUrl(recipeObject.opt("image"))

                            return Recipe(
                                title = recipeObject.optString("name", ""),
                                description = recipeObject.optString("description", ""),
                                imageUrl = imageUrl,
                                ingredients = ingredients,
                                steps = instructions
                            )
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors for this block and continue
                    }
                    startIndex = endIndex
                } else {
                    startIndex = -1 // Malformed tag
                }
            }
        }
        throw Exception("No valid 'Recipe' object found in any JSON-LD script tag.")
    }

    private fun findRecipeObject(jsonObject: JSONObject): JSONObject? {
        if (isRecipeType(jsonObject)) return jsonObject

        val graph = jsonObject.optJSONArray("@graph")
        if (graph != null) {
            for (i in 0 until graph.length()) {
                val node = graph.optJSONObject(i)
                if (node != null && isRecipeType(node)) {
                    return node
                }
            }
        }
        return null
    }

    private fun isRecipeType(jsonObject: JSONObject): Boolean {
        val type = jsonObject.opt("@type") ?: return false
        when (type) {
            is String -> return type == "Recipe"
            is JSONArray -> {
                for (i in 0 until type.length()) {
                    if (type.optString(i) == "Recipe") return true
                }
            }
        }
        return false
    }

    private fun parseImageUrl(imageNode: Any?): String? {
        val url = when (imageNode) {
            is JSONObject -> imageNode.optString("url") // Use the version without a default value
            is JSONArray -> if (imageNode.length() >0) parseImageUrl(imageNode.opt(0)) else null
            is String -> imageNode
            else -> null
        }
        // Return null if the url is blank, otherwise return the url.
        return url?.ifBlank { null }
    }

    private fun parseJsonArrayToStringList(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    private fun parseInstructionArray(jsonArray: JSONArray): List<String> {
        val instructions = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            when (val item = jsonArray.get(i)) {
                is JSONObject -> instructions.add(item.optString("text", ""))
                is String -> instructions.add(item)
            }
        }
        return instructions.filter { it.isNotBlank() }
    }

    fun navigationHandled() {
        _uiState.update { it.copy(newRecipeId = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
