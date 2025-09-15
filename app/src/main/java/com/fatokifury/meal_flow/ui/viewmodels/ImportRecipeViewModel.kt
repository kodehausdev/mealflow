//package com.fatokifury.meal_flow.ui.viewmodels
//
//import android.util.Log // Added for logging
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import io.ktor.client.HttpClient
//import io.ktor.client.call.body
//import io.ktor.client.engine.cio.CIO
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.client.request.get
//import io.ktor.serialization.kotlinx.json.json
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonElement
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.JsonArray // Ensure this is imported for type checking
//import kotlinx.serialization.json.JsonPrimitive // Ensure this is imported for type checking
//import kotlinx.serialization.json.jsonArray
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.jsonPrimitive
//import org.jsoup.Jsoup
//import javax.inject.Inject
//
//// Represents the state of the recipe import process
//data class ImportRecipeUiState(
//    val isLoading: Boolean = false,
//    val successNavigationArgs: ParsedRecipeArgs? = null,
//    val errorMessage: String? = null,
//    val importUrl: String? = null
//)
//
//// Data class to hold arguments for navigating to AddRecipeScreen
//data class ParsedRecipeArgs(
//    val title: String?,
//    val imageUrl: String?,
//    val description: String?,
//    val ingredients: List<String>?,
//    val steps: List<String>?,
//    val tags: List<String>?
//)
//
//// Minimalist data classes for parsing known JSON-LD recipe structures
//@Serializable
//data class JsonLdRecipe(
//    @SerialName("@context") val context: JsonElement? = null,
//    @SerialName("@type") val type: JsonElement? = null, // Can be string or array
//    @SerialName("name") val name: String? = null, // Made nullable, added "headline"
//    val description: String? = null, // Made nullable
//    val image: JsonElement? = null, // Keep as JsonElement for flexibility
//    @SerialName("recipeIngredient") val recipeIngredient: List<String>? = null, // Made nullable, added "ingredients"
//    @SerialName("recipeInstructions") val recipeInstructions: JsonElement? = null, // Keep as JsonElement, added aliases
//    @SerialName("keywords") val keywords: JsonElement? = null, // Keep as JsonElement, added aliases
//    // Optional common fields, made nullable
//    @SerialName("recipeYield") val recipeYield: JsonElement? = null,
//    val prepTime: String? = null,
//    val cookTime: String? = null,
//    val totalTime: String? = null,
//    val aggregateRating: AggregateRating? = null,
//    val nutrition: NutritionInformation? = null,
//    val author: JsonElement? = null // Can be String, Person object, or Array
//)
//
//@Serializable
//data class HowToStep(
//    @SerialName("@type") val type: String? = null, // Expect "HowToStep"
//    val text: String? = null, // Made nullable
//    val name: String? = null, // Optional step name, made nullable
//    val url: String? = null, // Optional URL for the step, made nullable
//    val image: JsonElement? = null // Optional image for the step
//)
//
//@Serializable
//data class ImageObject(
//    @SerialName("@type") val type: String? = null, // Expect "ImageObject"
//    val url: String? = null, // Made nullable
//    val height: Int? = null, // Made nullable
//    val width: Int? = null, // Made nullable
//    val caption: String? = null, // Added optional caption
//    val description: String? = null // Added optional description
//)
//
//// Optional supporting data classes, all fields nullable by default
//@Serializable
//data class AggregateRating(
//    val ratingValue: String? = null,
//    @SerialName("reviewCount") val reviewCount: String? = null,
//    val bestRating: String? = null,
//    val worstRating: String? = null
//)
//
//@Serializable
//data class NutritionInformation(
//    val calories: String? = null,
//    val servingSize: String? = null,
//    val fatContent: String? = null,
//    val saturatedFatContent: String? = null,
//    val carbohydrateContent: String? = null,
//    val sugarContent: String? = null,
//    val proteinContent: String? = null,
//    val cholesterolContent: String? = null,
//    val sodiumContent: String? = null
//    // Add other common nutrition fields if you encounter them
//)
//
//@Serializable
//data class Person(
//    @SerialName("@type") val type: String? = null, // Should be "Person" or "Organization"
//    val name: String? = null,
//    val url: String? = null // Optional URL for the author
//)
//
//
//
//@HiltViewModel
//class ImportRecipeViewModel @Inject constructor() : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ImportRecipeUiState())
//    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()
//
//    private val httpClient by lazy {
//        HttpClient(CIO) {
//            install(ContentNegotiation) {
//                json(Json {
//                    prettyPrint = true
//                    isLenient = true
//                    ignoreUnknownKeys = true
//                })
//            }
//        }
//    }
//
//    private val jsonParser = Json {
//        ignoreUnknownKeys = true
//        isLenient = true
//        coerceInputValues = true
//    }
//
//    fun startImport(url: String) {
//        _uiState.value = ImportRecipeUiState(isLoading = true, importUrl = url)
//        Log.d("ImportRecipeVM", "Starting import for URL: $url")
//        viewModelScope.launch {
//            try {
//                if (!url.startsWith("http://") && !url.startsWith("https://")) {
//                    Log.w("ImportRecipeVM", "Invalid URL scheme: $url")
//                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Invalid URL. Must start with http:// or https://")
//                    return@launch
//                }
//
//                val htmlContent = withContext(Dispatchers.IO) {
//                    httpClient.get(url).body<String>()
//                }
//                Log.d("ImportRecipeVM", "Fetched HTML content. Length: ${htmlContent.length}")
//
//                val document = Jsoup.parse(htmlContent)
//                val scriptElements = document.select("script[type=\"application/ld+json\"]")
//                Log.d("ImportRecipeVM", "Found ${scriptElements.size} ld+json script elements.")
//
//                var parsedRecipeArgs: ParsedRecipeArgs? = null
//
//                for ((index, element) in scriptElements.withIndex()) {
//                    val scriptContent = element.data()
//                    if (scriptContent.isBlank()) {
//                        Log.d("ImportRecipeVM", "Script #$index is blank.")
//                        continue
//                    }
//                    // Log only the first 500 chars of script content to avoid flooding Logcat
//                    Log.d("ImportRecipeVM", "Script #$index content (first 500 chars): ${scriptContent.take(500)}")
//
//                    try {
//                        val jsonElement = jsonParser.parseToJsonElement(scriptContent)
//                        Log.d("ImportRecipeVM", "Successfully parsed script #$index to JsonElement.")
//
//                        if (jsonElement is JsonObject) {
//                            Log.d("ImportRecipeVM", "Script #$index is a JsonObject. Attempting to parse as recipe.")
//                            parsedRecipeArgs = parseRecipeFromJsonObject(jsonElement)
//                            if (parsedRecipeArgs != null) {
//                                Log.d("ImportRecipeVM", "Successfully parsed recipe from script #$index (single object).")
//                                break
//                            }
//                        } else if (jsonElement is JsonArray && jsonElement.jsonArray.any()) { // Added check for JsonArray
//                            Log.d("ImportRecipeVM", "Script #$index is a JsonArray with ${jsonElement.jsonArray.size} items.")
//                            for ((itemIndex, item) in jsonElement.jsonArray.withIndex()) {
//                                if (item is JsonObject) {
//                                    val graph = item.jsonObject["@graph"]?.jsonArray
//                                    if (graph != null) {
//                                        Log.d("ImportRecipeVM", "Script #$index, item #$itemIndex is a @graph with ${graph.size} items.")
//                                        for ((graphIndex, graphItem) in graph.withIndex()) {
//                                            if (graphItem is JsonObject) {
//                                                Log.d("ImportRecipeVM", "Attempting to parse @graph item #$graphIndex as recipe.")
//                                                parsedRecipeArgs = parseRecipeFromJsonObject(graphItem)
//                                                if (parsedRecipeArgs != null) {
//                                                    Log.d("ImportRecipeVM", "Successfully parsed recipe from @graph item #$graphIndex.")
//                                                    break // from inner graph loop
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        Log.d("ImportRecipeVM", "Script #$index, item #$itemIndex is a JsonObject (not @graph). Attempting to parse as recipe.")
//                                        parsedRecipeArgs = parseRecipeFromJsonObject(item)
//                                    }
//                                    if (parsedRecipeArgs != null) {
//                                        Log.d("ImportRecipeVM", "Successfully parsed recipe from array item #$itemIndex.")
//                                        break // from item loop
//                                    }
//                                }
//                            }
//                            if (parsedRecipeArgs != null) break // from main script loop
//                        }
//                    } catch (e: Exception) {
//                        Log.e("ImportRecipeVM", "Error parsing script #$index: ${e.message}", e)
//                    }
//                }
//
//                if (parsedRecipeArgs != null) {
//                    Log.d("ImportRecipeVM", "Recipe parsed successfully: ${parsedRecipeArgs.title}")
//                    _uiState.value = _uiState.value.copy(isLoading = false, successNavigationArgs = parsedRecipeArgs)
//                } else {
//                    Log.w("ImportRecipeVM", "No recipe data found after checking all scripts.")
//                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No recipe data found on the page or failed to parse it.")
//                }
//
//            } catch (e: Exception) {
//                Log.e("ImportRecipeVM", "Generic error during import: ${e.message}", e)
//                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to fetch or parse recipe: ${e.message}")
//            }
//        }
//    }
//
//    private fun parseRecipeFromJsonObject(jsonObject: JsonObject): ParsedRecipeArgs? {
//        Log.d("ImportRecipeVM", "parseRecipeFromJsonObject called. Keys: ${jsonObject.keys}")
//        try {
//            val recipe = jsonParser.decodeFromJsonElement(JsonLdRecipe.serializer(), jsonObject)
//            Log.d("ImportRecipeVM", "Decoded JsonLdRecipe: name='${recipe.name}', type='${recipe.type?.toString()?.take(100)}'")
//
//            val typeElement = recipe.type
//            var isRecipeType = false
//            when (typeElement) {
//                is JsonPrimitive -> isRecipeType = typeElement.content == "Recipe"
//                is JsonArray -> isRecipeType = typeElement.jsonArray.any { it is JsonPrimitive && it.jsonPrimitive.content == "Recipe" }
//                is JsonObject -> TODO()
//                null -> TODO()
//            }
//
//            if (!isRecipeType) {
//                Log.d("ImportRecipeVM", "Not a 'Recipe' type. Type found: ${recipe.type?.toString()?.take(100)}")
//                return null
//            }
//            Log.d("ImportRecipeVM", "'Recipe' type confirmed for '${recipe.name}'.")
//
//            val title = recipe.name
//            val description = recipe.description
//            val ingredients = recipe.recipeIngredient
//            Log.d("ImportRecipeVM", "Parsed ingredients: ${ingredients?.size}")
//            val imageUrl = when (val img = recipe.image) {
//                is JsonObject -> jsonParser.decodeFromJsonElement(ImageObject.serializer(), img).url
//                is JsonArray -> img.firstOrNull()?.jsonObject?.let {
//                    jsonParser.decodeFromJsonElement(ImageObject.serializer(), it).url
//                } ?: img.firstOrNull()?.jsonPrimitive?.content
//                is JsonPrimitive -> img.content
//                else -> null
//            }
//            Log.d("ImportRecipeVM", "Parsed image URL: $imageUrl")
//
//            val stepsList = mutableListOf<String>()
//            when (val instructions = recipe.recipeInstructions) {
//                is JsonPrimitive -> stepsList.add(instructions.content)
//                is JsonArray -> {
//                    instructions.forEach { stepElement ->
//                        when {
//                            stepElement is JsonObject && stepElement["@type"]?.jsonPrimitive?.content == "HowToStep" -> {
//                                jsonParser.decodeFromJsonElement(HowToStep.serializer(), stepElement).text?.let { stepsList.add(it) }
//                            }
//                            stepElement is JsonObject -> {
//                                stepElement["text"]?.jsonPrimitive?.content?.let { stepsList.add(it) }
//                            }
//                            stepElement is JsonPrimitive -> stepsList.add(stepElement.content)
//                        }
//                    }
//                }
//                else -> { /* No instructions */ }
//            }
//            Log.d("ImportRecipeVM", "Parsed ${stepsList.size} steps.")
//
//            val tagsList = mutableListOf<String>()
//            when (val keywords = recipe.keywords) {
//                is JsonPrimitive -> tagsList.addAll(keywords.content.split(',').map { it.trim() }.filter { it.isNotEmpty() })
//                is JsonArray -> keywords.forEach { tagElement ->
//                    tagElement.jsonPrimitive.content.let { tagsList.add(it) }
//                }
//                else -> { /* No keywords */ }
//            }
//            Log.d("ImportRecipeVM", "Parsed ${tagsList.size} tags.")
//
//            if (title.isNullOrBlank() && ingredients.isNullOrEmpty() && stepsList.isEmpty()) {
//                Log.w("ImportRecipeVM", "Parsed recipe, but not enough data (title, ingredients, steps all empty). Title: $title")
//                return null
//            }
//
//            Log.d("ImportRecipeVM", "Successfully extracted args: Title='${title}'")
//            return ParsedRecipeArgs(
//                title = title?.trim(),
//                imageUrl = imageUrl?.trim(),
//                description = description?.trim(),
//                ingredients = ingredients?.map { it.trim() }?.filter { it.isNotEmpty() },
//                steps = stepsList.map { it.trim() }.filter { it.isNotEmpty() },
//                tags = tagsList.map { it.trim() }.filter { it.isNotEmpty() }
//            )
//        } catch (e: Exception) {
//            Log.e("ImportRecipeVM", "Error in parseRecipeFromJsonObject: ${e.message}", e)
//            return null
//        }
//    }





package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Log // Added for logging
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import javax.inject.Inject

// Represents the state of the recipe import process
data class ImportRecipeUiState(
    val isLoading: Boolean = false,
    val successNavigationArgs: ParsedRecipeArgs? = null,
    val errorMessage: String? = null,
    val importUrl: String? = null
)

// Data class to hold arguments for navigating to AddRecipeScreen
data class ParsedRecipeArgs(
    val title: String?,
    val imageUrl: String?,
    val description: String?,
    val ingredients: List<String>?,
    val steps: List<String>?,
    val tags: List<String>?
)

// Minimalist data classes for parsing known JSON-LD recipe structures
@Serializable
data class JsonLdRecipe(
    @SerialName("@context") val context: JsonElement? = null,
    @SerialName("@type") val type: JsonElement? = null, // Can be string or array
    @SerialName("name") val name: String? = null, // Made nullable, added "headline"
    val description: String? = null, // Made nullable
    val image: JsonElement? = null, // Keep as JsonElement for flexibility
    @SerialName("recipeIngredient") val recipeIngredient: List<String>? = null, // Made nullable, added "ingredients"
    @SerialName("recipeInstructions") val recipeInstructions: JsonElement? = null, // Keep as JsonElement, added aliases
    @SerialName("keywords") val keywords: JsonElement? = null, // Keep as JsonElement, added aliases
    // Optional common fields, made nullable
    @SerialName("recipeYield") val recipeYield: JsonElement? = null,
    val prepTime: String? = null,
    val cookTime: String? = null,
    val totalTime: String? = null,
    val aggregateRating: AggregateRating? = null,
    val nutrition: NutritionInformation? = null,
    val author: JsonElement? = null // Can be String, Person object, or Array
)

@Serializable
data class HowToStep(
    @SerialName("@type") val type: String? = null, // Expect "HowToStep"
    val text: String? = null, // Made nullable
    val name: String? = null, // Optional step name, made nullable
    val url: String? = null, // Optional URL for the step, made nullable
    val image: JsonElement? = null // Optional image for the step
)

@Serializable
data class ImageObject(
    @SerialName("@type") val type: String? = null, // Expect "ImageObject"
    val url: String? = null, // Made nullable
    val height: Int? = null, // Made nullable
    val width: Int? = null, // Made nullable
    val caption: String? = null, // Added optional caption
    val description: String? = null // Added optional description
)

// Optional supporting data classes, all fields nullable by default
@Serializable
data class AggregateRating(
    val ratingValue: String? = null,
    @SerialName("reviewCount") val reviewCount: String? = null,
    val bestRating: String? = null,
    val worstRating: String? = null
)

@Serializable
data class NutritionInformation(
    val calories: String? = null,
    val servingSize: String? = null,
    val fatContent: String? = null,
    val saturatedFatContent: String? = null,
    val carbohydrateContent: String? = null,
    val sugarContent: String? = null,
    val proteinContent: String? = null,
    val cholesterolContent: String? = null,
    val sodiumContent: String? = null
    // Add other common nutrition fields if you encounter them
)

@Serializable
data class Person(
    @SerialName("@type") val type: String? = null, // Should be "Person" or "Organization"
    val name: String? = null,
    val url: String? = null // Optional URL for the author
)


@HiltViewModel
class ImportRecipeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ImportRecipeUiState())
    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true // Good for minor type mismatches if possible
                })
            }
        }
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun startImport(url: String) {
        _uiState.value = ImportRecipeUiState(isLoading = true, importUrl = url)
        Log.d("ImportRecipeVM", "Starting import for URL: $url")
        viewModelScope.launch {
            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Log.w("ImportRecipeVM", "Invalid URL scheme: $url")
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Invalid URL. Must start with http:// or https://")
                    return@launch
                }

                val htmlContent = withContext(Dispatchers.IO) {
                    httpClient.get(url).body<String>()
                }
                Log.d("ImportRecipeVM", "Fetched HTML content. Length: ${htmlContent.length}")

                val document = Jsoup.parse(htmlContent)
                val scriptElements = document.select("script[type=\"application/ld+json\"]")
                Log.d("ImportRecipeVM", "Found ${scriptElements.size} ld+json script elements.")

                var parsedRecipeArgs: ParsedRecipeArgs? = null

                for ((index, element) in scriptElements.withIndex()) {
                    val scriptContent = element.data()
                    if (scriptContent.isBlank()) {
                        Log.d("ImportRecipeVM", "Script #$index is blank.")
                        continue
                    }
                    Log.d("ImportRecipeVM", "Script #$index content (first 500 chars): ${scriptContent.take(500)}")

                    try {
                        val jsonElementRoot = jsonParser.parseToJsonElement(scriptContent)
                        Log.d("ImportRecipeVM", "Successfully parsed script #$index to JsonElement: ${jsonElementRoot::class.simpleName}")

                        val potentialRecipeObjects = mutableListOf<JsonObject>()

                        when (jsonElementRoot) {
                            is JsonObject -> {
                                val graphArray = jsonElementRoot["@graph"]?.jsonArray
                                if (graphArray != null) {
                                    Log.d("ImportRecipeVM", "Script #$index is JsonObject with @graph (${graphArray.size} items).")
                                    graphArray.forEachIndexed { graphItemIndex, graphItem ->
                                        if (graphItem is JsonObject) {
                                            potentialRecipeObjects.add(graphItem)
                                        } else {
                                            Log.d("ImportRecipeVM", "@graph item #$graphItemIndex not JsonObject.")
                                        }
                                    }
                                } else {
                                    Log.d("ImportRecipeVM", "Script #$index is JsonObject (no @graph). Adding root as potential.")
                                    potentialRecipeObjects.add(jsonElementRoot)
                                }
                            }
                            is JsonArray -> {
                                Log.d("ImportRecipeVM", "Script #$index is JsonArray (${jsonElementRoot.size} items).")
                                jsonElementRoot.forEachIndexed { arrayItemIndex, arrayItem ->
                                    if (arrayItem is JsonObject) {
                                        potentialRecipeObjects.add(arrayItem)
                                    } else {
                                        Log.d("ImportRecipeVM", "JsonArray item #$arrayItemIndex not JsonObject.")
                                    }
                                }
                            }
                            else -> Log.w("ImportRecipeVM", "Script #$index (decoded) is neither JsonObject nor JsonArray.")
                        }

                        if (potentialRecipeObjects.isEmpty()) {
                            Log.w("ImportRecipeVM", "No potential JsonObject(s) found in script #$index.")
                            continue
                        } else {
                            Log.d("ImportRecipeVM", "Found ${potentialRecipeObjects.size} potential recipe JsonObject(s) in script #$index.")
                        }


                        for ((objIndex, recipeJsonObj) in potentialRecipeObjects.withIndex()) {
                            Log.d("ImportRecipeVM", "Checking potential recipe object #$objIndex from script #$index. Keys: ${recipeJsonObj.keys.take(10)}...")

                            val typeElement = recipeJsonObj["@type"]
                            var isRecipeType = false
                            when (typeElement) {
                                is JsonPrimitive -> isRecipeType = typeElement.content.contains("Recipe", ignoreCase = true)
                                is JsonArray -> isRecipeType = typeElement.any { it is JsonPrimitive && it.jsonPrimitive.content.contains("Recipe", ignoreCase = true) }
                                null -> Log.d("ImportRecipeVM", "Object #$objIndex has no @type field.")
                                else -> Log.d("ImportRecipeVM", "Object #$objIndex @type is neither Primitive nor Array: ${typeElement::class.simpleName}")
                            }

                            if (isRecipeType) {
                                Log.i("ImportRecipeVM", "Object #$objIndex IS 'Recipe' type. Attempting full parse...")
                                parsedRecipeArgs = parseRecipeFromJsonObject(recipeJsonObj)
                                if (parsedRecipeArgs != null) {
                                    Log.i("ImportRecipeVM", "Successfully parsed recipe from object #$objIndex in script #$index. Title: ${parsedRecipeArgs.title}")
                                    break // from potentialRecipeObjects loop
                                } else {
                                    Log.w("ImportRecipeVM", "Object #$objIndex was 'Recipe' type, but parseRecipeFromJsonObject returned null.")
                                }
                            } else {
                                Log.d("ImportRecipeVM", "Object #$objIndex is NOT 'Recipe' type. Skipping full parse.")
                            }
                        }

                        if (parsedRecipeArgs != null) {
                            break // from scriptElements loop
                        }

                    } catch (e: kotlinx.serialization.SerializationException) {
                        Log.e("ImportRecipeVM", "Serialization error processing script #$index: ${e.message}", e)
                    } catch (e: Exception) {
                        Log.e("ImportRecipeVM", "Generic error processing script #$index: ${e.message}", e)
                    }
                }

                if (parsedRecipeArgs != null) {
                    Log.i("ImportRecipeVM", "Recipe parsed successfully: ${parsedRecipeArgs.title}")
                    _uiState.value = _uiState.value.copy(isLoading = false, successNavigationArgs = parsedRecipeArgs)
                } else {
                    Log.w("ImportRecipeVM", "No recipe data found after checking all scripts and potential objects.")
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No recipe data found on the page or failed to parse it.")
                }

            } catch (e: Exception) {
                Log.e("ImportRecipeVM", "Critical error during import: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to fetch or parse recipe: ${e.message}")
            }
        }
    }

    private fun parseRecipeFromJsonObject(jsonObject: JsonObject): ParsedRecipeArgs? {
        Log.d("ImportRecipeVM", "parseRecipeFromJsonObject called. Candidate object keys: ${jsonObject.keys.take(10)}...")
        try {
            val recipe = jsonParser.decodeFromJsonElement(JsonLdRecipe.serializer(), jsonObject)
            // Log essential fields from the decoded recipe to verify parsing
            Log.d("ImportRecipeVM", "Decoded JsonLdRecipe: name='${recipe.name}', type='${recipe.type?.toString()?.take(100)}', ingredients_count='${recipe.recipeIngredient?.size}', instructions_type='${recipe.recipeInstructions?.let { it::class.simpleName }}'")


            // Type check is technically done before calling, but as a safeguard or if called directly:
            val typeElement = recipe.type
            var isRecipeType = false
            when (typeElement) {
                is JsonPrimitive -> isRecipeType = typeElement.content.contains("Recipe", ignoreCase = true)
                is JsonArray -> isRecipeType = typeElement.any { it is JsonPrimitive && it.jsonPrimitive.content.contains("Recipe", ignoreCase = true) }
                else -> {} // No action if type is null or JsonObject
            }

            if (!isRecipeType) { // This check should ideally be redundant if called from the new loop logic
                Log.w("ImportRecipeVM", "parseRecipeFromJsonObject: Decoded object not 'Recipe' type. Actual type: ${recipe.type?.toString()?.take(100)}")
                return null
            }
            Log.d("ImportRecipeVM", "'Recipe' type confirmed for '${recipe.name ?: "Unknown name"}'.")


            val title = recipe.name?.trim()?.takeIf { it.isNotEmpty() }
            val description = recipe.description?.trim()?.takeIf { it.isNotEmpty() }
            val ingredients = recipe.recipeIngredient
                ?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
                ?.filter { it.isNotEmpty() }

            Log.d("ImportRecipeVM", "Title: '$title', Ingredients count: ${ingredients?.size ?: "0 or null"}")

            val imageUrl = when (val imgElement = recipe.image) {
                is JsonPrimitive -> imgElement.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
                is JsonObject -> {
                    try { jsonParser.decodeFromJsonElement(ImageObject.serializer(), imgElement).url?.trim()?.takeIf { it.isNotBlank() } }
                    catch (e: Exception) { Log.w("ImportRecipeVM", "Failed to decode image JsonObject: $imgElement", e); null }
                }
                is JsonArray -> imgElement.firstNotNullOfOrNull { arrayEl ->
                    when (arrayEl) {
                        is JsonPrimitive -> arrayEl.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
                        is JsonObject -> {
                            try { jsonParser.decodeFromJsonElement(ImageObject.serializer(), arrayEl).url?.trim()?.takeIf { it.isNotBlank() } }
                            catch (e: Exception) { null }
                        }
                        else -> null
                    }
                }
                null -> null
            }
            Log.d("ImportRecipeVM", "Parsed image URL: $imageUrl")

            val stepsList = mutableListOf<String>()
            when (val instructionsElement = recipe.recipeInstructions) {
                is JsonPrimitive -> instructionsElement.contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let { stepsList.add(it) }
                is JsonObject -> {
                    val stepText = if (instructionsElement["@type"]?.jsonPrimitive?.contentOrNull == "HowToStep") {
                        try { jsonParser.decodeFromJsonElement(HowToStep.serializer(), instructionsElement).text }
                        catch (e: Exception) { Log.w("ImportRecipeVM", "Failed to decode single HowToStep JsonObject: $instructionsElement", e); null }
                    } else {
                        instructionsElement["text"]?.jsonPrimitive?.contentOrNull // Generic text from object
                    }
                    stepText?.trim()?.takeIf { it.isNotBlank() }?.let { stepsList.add(it) }
                }
                is JsonArray -> {
                    instructionsElement.forEach { stepElement ->
                        val stepText = when (stepElement) {
                            is JsonPrimitive -> stepElement.contentOrNull
                            is JsonObject -> {
                                if (stepElement["@type"]?.jsonPrimitive?.contentOrNull == "HowToStep") {
                                    try { jsonParser.decodeFromJsonElement(HowToStep.serializer(), stepElement).text }
                                    catch (e: Exception) { Log.w("ImportRecipeVM", "Failed to decode HowToStep from array: $stepElement", e); null }
                                } else {
                                    stepElement["text"]?.jsonPrimitive?.contentOrNull
                                }
                            }
                            else -> null
                        }
                        stepText?.trim()?.takeIf { it.isNotBlank() }?.let { stepsList.add(it) }
                    }
                }
                null -> { /* No instructions */ }
            }
            Log.d("ImportRecipeVM", "Parsed ${stepsList.size} steps.")

            val tagsList = mutableListOf<String>()
            when (val keywordsElement = recipe.keywords) {
                is JsonPrimitive -> keywordsElement.contentOrNull?.split(',')?.forEach { tag ->
                    tag.trim().takeIf { it.isNotEmpty() }?.let { tagsList.add(it) }
                }
                is JsonArray -> keywordsElement.forEach { tagElement ->
                    tagElement.jsonPrimitive.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }?.let { tagsList.add(it) }
                }
                is JsonObject -> Log.w("ImportRecipeVM", "Keywords field was an unexpected JsonObject: $keywordsElement")
                null -> { /* No keywords */ }
            }
            Log.d("ImportRecipeVM", "Parsed ${tagsList.size} tags.")

            if (title.isNullOrBlank() && ingredients.isNullOrEmpty() && stepsList.isEmpty()) {
                Log.w("ImportRecipeVM", "Parsed recipe, but not enough substantial data (title, ingredients, steps all empty/null). Discarding.")
                return null
            }

            Log.i("ImportRecipeVM", "Successfully extracted ParsedRecipeArgs: Title='${title}'")
            return ParsedRecipeArgs(title, imageUrl, description, ingredients, stepsList, tagsList)

        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e("ImportRecipeVM", "SERIALIZATION error in parseRecipeFromJsonObject for object. Keys: ${jsonObject.keys.take(10)} : ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e("ImportRecipeVM", "GENERIC error in parseRecipeFromJsonObject for object. Keys: ${jsonObject.keys.take(10)} : ${e.message}", e)
            return null
        }
    }


    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun navigationHandled() {
        _uiState.value = _uiState.value.copy(successNavigationArgs = null)
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
