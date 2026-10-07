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
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import org.json.JSONTokener
import org.json.JSONArray

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

    private val ldJsonRegex = Regex(
        """<script[^>]*type\s*=\s*["']?application/ld\+json["']?[^>]*>(.*?)</script>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private fun parseJsonLd(htmlContent: String): Recipe {
        val scripts = ldJsonRegex.findAll(htmlContent).map { it.groupValues[1].trim() }.toList()
        Log.d(TAG, "Found ${scripts.size} JSON-LD scripts")

        for ((i, raw) in scripts.withIndex()) {
            try {
                // JSONTokener returns JSONObject OR JSONArray depending on the content
                val root = JSONTokener(raw).nextValue()
                val recipe = findRecipeObject(root) ?: continue

                val ingredients = recipe.optJSONArray("recipeIngredient")
                    ?.let { parseIngredients(it) } ?: emptyList()

                val steps = when (val ins = recipe.opt("recipeInstructions")) {
                    is JSONArray -> parseInstructionArray(ins)
                    is String -> ins.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    else -> emptyList()
                }

                return Recipe(
                    title = recipe.optString("name", "").ifBlank { "Untitled Recipe" },
                    description = recipe.optString("description", ""),
                    imageUrl = parseImageUrl(recipe.opt("image")),
                    servings = parseServings(recipe.opt("recipeYield")),
                    ingredients = ingredients,
                    steps = steps
                )
            } catch (e: Exception) {
                Log.w(TAG, "✗ Error parsing JSON-LD script #${i + 1}: ${e.message}")
            }
        }
        throw Exception("No valid 'Recipe' object found in any JSON-LD script tag.")
    }

    private fun findRecipeObject(node: Any?): JSONObject? {
        when (node) {
            is JSONArray -> {
                for (i in 0 until node.length()) findRecipeObject(node.opt(i))?.let { return it }
            }
            is JSONObject -> {
                val type = node.opt("@type")
                val isRecipe = when (type) {
                    is String -> type.equals("Recipe", ignoreCase = true)
                    is JSONArray -> (0 until type.length()).any { type.optString(it).equals("Recipe", true) }
                    else -> false
                }
                if (isRecipe) return node
                findRecipeObject(node.opt("@graph"))?.let { return it }
                // Occasionally nested under mainEntity / mainEntityOfPage
                findRecipeObject(node.opt("mainEntity"))?.let { return it }
            }
        }
        return null
    }

    private fun parseServings(yield: Any?): Int {
        val text = when (yield) {
            is JSONArray -> (0 until yield.length()).joinToString(" ") { yield.optString(it) }
            null -> ""
            else -> yield.toString()
        }
        return Regex("\\d+").find(text)?.value?.toIntOrNull() ?: 1
    }
    // In ImportRecipeViewModel.kt
    private fun parseIngredients(jsonArray: JSONArray): List<Ingredient> {
        return (0 until jsonArray.length()).mapNotNull { i ->
            val rawString = jsonArray.optString(i)
            if (rawString.isBlank()) return@mapNotNull null

            val parts = rawString.trim().split(" ")

            // Try to parse "quantity unit name" format
            val quantity = parts.firstOrNull()?.toDoubleOrNull()

            if (quantity != null && parts.size >= 2) {
                // It looks like "1.5 cup flour"
                val unit = parts.getOrNull(1) ?: ""
                val name = if (parts.size > 2) parts.subList(2, parts.size).joinToString(" ") else ""
                Ingredient(name = name, quantity = quantity, unit = unit)
            } else {
                // Could not parse quantity, treat the whole line as the name
                Ingredient(name = rawString, quantity = 1.0, unit = "") // Fallback to old behavior
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
            is JSONObject -> imageNode.optString("url", "")
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