package com.fatokifury.meal_flow.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Ingredient
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

private const val TAG = "ImportRecipeViewModel"

/** The page had no JSON-LD at all (bot check, login wall, empty page). */
class NoStructuredDataException : Exception("No JSON-LD scripts found in page.")

/** The page had JSON-LD, but none of it described a Recipe. */
class NoRecipeFoundException : Exception("No valid 'Recipe' object found in any JSON-LD script tag.")

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

    private val requestQueue by lazy { Volley.newRequestQueue(getApplication<Application>()) }
    private val webViewFetcher by lazy { WebViewHtmlFetcher(getApplication()) }

    private val ldJsonRegex = Regex(
        """<script[^>]*type\s*=\s*["']?application/ld\+json["']?[^>]*>(.*?)</script>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    // ------------------------------------------------------------------
    // Entry point
    // ------------------------------------------------------------------

    init {
        val encodedUrl: String? = savedStateHandle["url"]

        if (encodedUrl == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No URL was provided.") }
        } else {
            val url = try {
                URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                    .substringBefore('#')
                    .trim()
            } catch (e: Exception) {
                Log.e(TAG, "URL Decode Error: ${e.message}", e)
                null
            }

            if (url == null || !url.startsWith("http", ignoreCase = true)) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "That doesn't look like a valid link.")
                }
            } else {
                _uiState.update { it.copy(isLoading = true, importUrl = url) }
                fetchHtml(url)
            }
        }
    }

    override fun onCleared() {
        requestQueue.stop()
        super.onCleared()
    }

    // ------------------------------------------------------------------
    // Fetching: Volley first, WebView fallback on 401/403
    // ------------------------------------------------------------------

    private fun fetchHtml(url: String) {
        val host = hostOf(url)

        fun isBlockedStatus(code: Int?, error: VolleyError): Boolean =
            code in setOf(401, 402, 403, 429) || error is AuthFailureError



        val request = object : StringRequest(
            Method.GET, url,
            { response ->
                Log.d(TAG, "HTML Response received, length: ${response.length}")
                parseHtmlAndSave(response)
            },
            { error ->
                val code = error.networkResponse?.statusCode
                Log.e(TAG, "Volley error: HTTP $code for $url", error)


                if (isBlockedStatus(code, error)) {
                        loadWithWebView(url)
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = volleyErrorMessage(error, host),
                                debugInfo = "HTTP $code\n${error.stackTraceToString()}"
                            )
                        }
                    }
                }

        ) {
            override fun getHeaders(): MutableMap<String, String> = hashMapOf(
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14; SM-A055F) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Accept-Language" to "en-US,en;q=0.9",
                "Upgrade-Insecure-Requests" to "1",
                "Sec-Fetch-Dest" to "document",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Site" to "none",
                "Sec-Fetch-User" to "?1"
            )
        }.apply {
            // 20s timeout, no retries: fail fast so the fallback can start.
            retryPolicy = DefaultRetryPolicy(20_000, 0, 1f)
        }

        requestQueue.add(request)
    }


    private val imageFetcher by lazy { WebViewImageFetcher(getApplication()) }

    /** Replaces a remote image URL with a local copy. On any failure, keeps the remote URL. */
    private suspend fun localizeImage(recipe: Recipe, pageUrl: String?): Recipe {
        val remote = recipe.imageUrl ?: return recipe
        if (!remote.startsWith("https://")) return recipe

        val bytes = imageFetcher.fetchAsJpeg(remote, referer = pageUrl ?: remote)
        if (bytes == null) {
            Log.w(TAG, "Could not download image, keeping remote URL: $remote")
            return recipe
        }

        val localUri = withContext(Dispatchers.IO) {
            val dir = File(getApplication<Application>().filesDir, "recipe_images").apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            file.writeBytes(bytes)
            Uri.fromFile(file).toString()
        }
        Log.d(TAG, "Image saved locally: $localUri (${bytes.size / 1024} KB)")
        return recipe.copy(imageUrl = localUri)
    }

    private fun loadWithWebView(url: String) {
        val host = hostOf(url)
        Log.d(TAG, "Falling back to WebView for $url")

        viewModelScope.launch {
            try {
                val html = webViewFetcher.fetch(url)
                Log.d(TAG, "WebView HTML received, length: ${html.length}")
                parseHtmlAndSave(html)
            } catch (e: WebViewHtmlFetcher.FetchFailedException) {
                Log.e(TAG, "WebView fetch failed: ${e.message} (HTTP ${e.httpStatus})", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (e.httpStatus == 403 || e.httpStatus == 401)
                            "$host blocked the import, even in browser mode. Try a different recipe site."
                        else
                            "Couldn't load the page from $host. Check your connection and try again.",
                        debugInfo = e.stackTraceToString()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "WebView fetch error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Something went wrong importing from $host.",
                        debugInfo = e.stackTraceToString()
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Parse + save
    // ------------------------------------------------------------------

    private fun parseHtmlAndSave(htmlContent: String) {
        val host = hostOf(_uiState.value.importUrl)

        viewModelScope.launch {
            try {
                // Regex + JSON over ~300 KB of HTML: keep it off the main thread.
                val parsed = withContext(Dispatchers.Default) { parseJsonLd(htmlContent) }
                val recipe = localizeImage(parsed, _uiState.value.importUrl)
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
                        errorMessage = parseErrorMessage(e, host),
                        debugInfo = e.stackTraceToString()
                    )
                }
            }
        }
    }

    private fun parseJsonLd(htmlContent: String): Recipe {
        val scripts = ldJsonRegex.findAll(htmlContent).map { it.groupValues[1].trim() }.toList()
        Log.d(TAG, "Found ${scripts.size} JSON-LD scripts")
        if (scripts.isEmpty()) throw NoStructuredDataException()

        for ((i, raw) in scripts.withIndex()) {
            try {
                // JSONTokener returns JSONObject OR JSONArray depending on the content
                val root = JSONTokener(raw).nextValue()
                val recipe = findRecipeObject(root) ?: continue
                Log.d(TAG, "image node = ${recipe.opt("image")} | thumbnailUrl = ${recipe.opt("thumbnailUrl")}")

                val ingredients = recipe.optJSONArray("recipeIngredient")
                    ?.let { parseIngredients(it) } ?: emptyList()

                val steps = when (val ins = recipe.opt("recipeInstructions")) {
                    is JSONArray -> parseInstructionArray(ins)
                    is String -> ins.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    else -> emptyList()
                }

                val finalImage = parseImageUrl(recipe.opt("image"))
                Log.d(TAG, "FINAL imageUrl = $finalImage")

                return Recipe(
                    title = recipe.optString("name", "").ifBlank { "Untitled Recipe" },
                    description = recipe.optString("description", ""),
                    imageUrl = parseImageUrl(recipe.opt("image"))
                        ?: parseImageUrl(recipe.opt("thumbnailUrl"))
                        ?: ogImage(htmlContent),
                    servings = parseServings(recipe.opt("recipeYield")),
                    ingredients = ingredients,
                    steps = steps
                )
            } catch (e: Exception) {
                Log.w(TAG, "✗ Error parsing JSON-LD script #${i + 1}: ${e.message}")
            }
        }
        throw NoRecipeFoundException()
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
                // Occasionally nested under mainEntity
                findRecipeObject(node.opt("mainEntity"))?.let { return it }
            }
        }
        return null
    }

    // ------------------------------------------------------------------
    // Field parsers
    // ------------------------------------------------------------------

    private fun parseServings(yield: Any?): Int {
        val text = when (yield) {
            is JSONArray -> (0 until yield.length()).joinToString(" ") { yield.optString(it) }
            null -> ""
            else -> yield.toString()
        }
        return Regex("\\d+").find(text)?.value?.toIntOrNull() ?: 1
    }

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
                Ingredient(name = rawString, quantity = 1.0, unit = "")
            }
        }
    }

    private fun parseImageUrl(node: Any?): String? {
        val raw: String? = when (node) {
            is String -> node
            is JSONObject -> node.optString("url")
                .ifBlank { node.optString("contentUrl") }
                .ifBlank { node.optString("@id") }
            is JSONArray -> (0 until node.length()).firstNotNullOfOrNull { parseImageUrl(node.opt(it)) }
            else -> null
        }
        return normalizeImageUrl(raw)
    }

    private fun normalizeImageUrl(raw: String?): String? {
        val u = raw?.trim().orEmpty()
        return when {
            u.startsWith("https://") -> u
            u.startsWith("//") -> "https:$u"
            u.startsWith("http://") -> "https://" + u.removePrefix("http://")
            else -> null   // blank, relative path, or "#fragment" id
        }
    }

    private val ogImageRegexes = listOf(
        Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""", RegexOption.IGNORE_CASE)
    )

    private fun ogImage(html: String): String? =
        ogImageRegexes.firstNotNullOfOrNull { it.find(html)?.groupValues?.get(1) }
            .let { normalizeImageUrl(it) }

    private fun parseInstructionArray(jsonArray: JSONArray): List<String> {
        val instructions = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            when (val item = jsonArray.opt(i)) {
                is String -> instructions.add(item)
                is JSONObject -> {
                    // HowToSection wraps its steps in itemListElement
                    val nested = item.optJSONArray("itemListElement")
                    if (nested != null) instructions.addAll(parseInstructionArray(nested))
                    else instructions.add(item.optString("text", ""))
                }
            }
        }
        return instructions.map { it.trim() }.filter { it.isNotBlank() }
    }

    // ------------------------------------------------------------------
    // Error messages
    // ------------------------------------------------------------------

    private fun hostOf(url: String?): String =
        url?.let { Uri.parse(it).host }?.removePrefix("www.") ?: "The site"

    private fun volleyErrorMessage(error: VolleyError, host: String): String {
        val code = error.networkResponse?.statusCode
        return when {
            code == 401 || code == 403 || error is AuthFailureError ->
                "$host blocked the import (HTTP ${code ?: 403}). This site doesn't allow automated access."
            code == 404 -> "Recipe not found (HTTP 404). Check that the link is correct."
            code == 429 -> "Too many requests to $host. Wait a minute and try again."
            code != null && code in 400..499 -> "$host rejected the request (HTTP $code)."
            error is ServerError -> "$host is having problems right now (HTTP ${code ?: "5xx"}). Try again later."
            error is TimeoutError -> "The request to $host timed out. Check your connection and try again."
            error is NoConnectionError -> "No internet connection."
            error is NetworkError -> "A network error occurred. Check your connection."
            error is ParseError -> "The response from $host couldn't be read."
            else -> "Couldn't reach $host: ${error.message ?: "unknown error"}"
        }
    }

    private fun parseErrorMessage(e: Throwable, host: String): String = when (e) {
        is NoStructuredDataException ->
            "$host didn't return a readable page (it may have shown a bot check or login wall)."
        is NoRecipeFoundException ->
            "That page doesn't contain a recipe MealFlow can import. Make sure the link goes directly to a recipe."
        is JSONException -> "The recipe data on $host was in an unexpected format."
        else -> "Something went wrong importing from $host."
    }

    // ------------------------------------------------------------------
    // UI actions
    // ------------------------------------------------------------------

    fun onNavigateBack() {
        navigationService.goBack()
    }

    fun navigateToRecipe(recipeId: String) {
        navigationService.navigateAndPopUp(
            Screen.AddRecipe.createRoute(recipeId),
            Screen.ImportRecipe.route
        )
    }

    fun navigationHandled() {
        _uiState.update { it.copy(newRecipeId = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
