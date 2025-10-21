package com.fatokifury.meal_flow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "ImportRecipeViewModel"

data class ImportRecipeUiState(
    val isLoading: Boolean = false,
    val importUrl: String? = null,
    val errorMessage: String? = null,
    val newRecipeId: String? = null,
    val debugInfo: String? = null
)

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    application: Application,
    private val navigationService: NavigationService,
    private val recipeRepository: RecipeRepository,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ImportRecipeUiState())
    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()

    init {
        val encodedUrl: String? = savedStateHandle["url"]

        if (encodedUrl == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No URL was provided.") }
        } else {
            try {
                val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                _uiState.update { it.copy(isLoading = true, importUrl = url) }

                val requestQueue = Volley.newRequestQueue(getApplication())
                val stringRequest = object : StringRequest(
                    Method.GET, url,
                    { response ->
                        Log.d(TAG, "HTML Response received, length: ${response.length}")
                        parseHtmlAndSave(response)
                    },
                    { error ->
                        Log.e(TAG, "Volley Error: ${error.message}", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to fetch URL: ${error.message}"
                            )
                        }
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["User-Agent"] =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
                        return headers
                    }
                }
                requestQueue.add(stringRequest)
            } catch (e: Exception) {
                Log.e(TAG, "URL Decode Error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "The provided URL is invalid."
                    )
                }
            }
        }
    }

    fun onNavigateBack() {
        navigationService.goBack()
    }

    fun navigateToRecipe(recipeId: String) {
        navigationService.navigateAndPopUp(
            Screen.AddRecipe.createRoute(recipeId),
            Screen.ImportRecipe.route
        )
    }

    private fun parseHtmlAndSave(htmlContent: String) {
        viewModelScope.launch {
            try {
                val recipe = parseJsonLd(htmlContent)
                val result = recipeRepository.saveRecipe(recipe)

                result.onSuccess { newId ->
                    Log.d(TAG, "Recipe saved successfully with ID: $newId")
                    _uiState.update { ui -> ui.copy(isLoading = false, newRecipeId = newId) }
                }.onFailure { exception ->
                    Log.e(TAG, "Repository save failed: ${exception.message}", exception)
                    _uiState.update { ui ->
                        ui.copy(
                            isLoading = false,
                            errorMessage = "Failed to save imported recipe: ${exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Parsing failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to parse recipe data: ${e.message}",
                        debugInfo = e.stackTraceToString()
                    )
                }
            }
        }
    }

    private fun parseJsonLd(htmlContent: String): Recipe {
        val patterns = listOf(
            "<script type=\"application/ld+json\">" to "</script>",
            "<script type='application/ld+json'>" to "</script>",
            "<script type=application/ld+json>" to "</script>",
            """<script[^>]*type\s*=\s*["\']?application/ld\+json["\']?[^>]*>""".toRegex() to "</script>"
        )

        var foundScripts = 0

        for ((startPattern, endPattern) in patterns) {
            var startIndex = 0

            while (startIndex != -1) {
                startIndex = when (startPattern) {
                    is String -> htmlContent.indexOf(startPattern, startIndex)
                    is Regex -> {
                        val match = startPattern.find(htmlContent, startIndex)
                        match?.range?.first ?: -1
                    }

                    else -> -1
                }

                if (startIndex != -1) {
                    foundScripts++
                    val scriptStart = when (startPattern) {
                        is String -> startIndex + startPattern.length
                        is Regex -> {
                            val match = startPattern.find(htmlContent, startIndex)
                            (match?.range?.last ?: startIndex) + 1
                        }

                        else -> startIndex
                    }

                    val endIndex = htmlContent.indexOf(endPattern, scriptStart)
                    if (endIndex != -1) {
                        val jsonLdContent = htmlContent.substring(scriptStart, endIndex).trim()

                        try {
                            val jsonObject = JSONObject(jsonLdContent)
                            val recipeObject = findRecipeObject(jsonObject)

                            if (recipeObject != null) {
                                val ingredients = recipeObject.optJSONArray("recipeIngredient")?.let { parseIngredients(it) } ?: emptyList()
                                val instructions = recipeObject.optJSONArray("recipeInstructions")?.let { parseInstructionArray(it) } ?: emptyList()
                                val imageUrl = parseImageUrl(recipeObject.opt("image"))
                                val servings = recipeObject.optString("recipeYield", "1").toIntOrNull() ?: 1

                                return Recipe(
                                    title = recipeObject.optString("name", "").ifBlank { "Untitled Recipe" },
                                    description = recipeObject.optString("description", ""),
                                    imageUrl = imageUrl,
                                    servings = servings,
                                    ingredients = ingredients,
                                    steps = instructions
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "✗ Error parsing JSON-LD script #$foundScripts: ${e.message}")
                        }
                        startIndex = endIndex
                    } else {
                        startIndex = -1
                    }
                }
            }
        }

        throw Exception("No valid 'Recipe' object found in any JSON-LD script tag.")
    }

    private fun parseIngredients(jsonArray: JSONArray): List<Ingredient> {
        return (0 until jsonArray.length()).mapNotNull { i ->
            val rawString = jsonArray.optString(i)
            if (rawString.isNotBlank()) {
                Ingredient(name = rawString, quantity = 1.0, unit = "unit")
            } else {
                null
            }
        }
    }

    private fun findRecipeObject(jsonObject: JSONObject): JSONObject? {
        if (isRecipeType(jsonObject)) {
            return jsonObject
        }

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
        return when (val type = jsonObject.opt("@type")) {
            is String -> type == "Recipe"
            is JSONArray -> {
                for (i in 0 until type.length()) {
                    if (type.optString(i) == "Recipe") return true
                }
                false
            }
            else -> false
        }
    }

    private fun parseImageUrl(imageNode: Any?): String? {
        return when (imageNode) {
            is JSONObject -> imageNode.optString("url", null)
            is JSONArray -> if (imageNode.length() > 0) parseImageUrl(imageNode.opt(0)) else null
            is String -> imageNode
            else -> null
        }?.ifBlank { null }
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