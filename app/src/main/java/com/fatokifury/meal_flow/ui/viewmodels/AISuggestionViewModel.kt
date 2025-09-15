package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
class AISuggestionViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // TODO: Implement AI Suggestion logic:
    // - Fetch recent meals to provide context for Gemini
    // - Call Gemini API with the prompt template
    // - Parse and display suggestions
    // - Handle loading and error states

    fun getMealSuggestions(pastMealsContext: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val systemPrompt = "You are MealFlow, a family meal assistant. You analyze past meals and suggest different meals for the next day. Suggestions should be realistic, simple, and culturally aware (Nigerian + global). Keep them short (2–5 words)."
                val userPrompt = """
                    Here are the last meals my family ate:
                    $pastMealsContext
                    Please suggest 2 different meal ideas for tomorrow. Format as a simple bullet list.
                """.trimIndent()
                
                // For more complex prompts with system instructions, roles etc.
                // val chat = generativeModel.startChat(
                //    history = listOf(
                //        content(role = "user") { text(systemPrompt) }, 
                //        content(role = "model") {text("Okay, I understand. I'm MealFlow, ready to suggest meals.")}
                //        )
                // )
                // val response: GenerateContentResponse = chat.sendMessage(userPrompt)


                val response: GenerateContentResponse = generativeModel.generateContent(systemPrompt + "\n" + userPrompt) // Simpler for now

                val responseText = response.text
                if (responseText != null) {
                    _suggestions.value = responseText.lines().mapNotNull { line ->
                        line.removePrefix("*").removePrefix("-").trim().takeIf { it.isNotBlank() }
                    }
                } else {
                    _error.value = "No suggestions received."
                }
            } catch (e: Exception) {
                _error.value = "Error fetching suggestions: ${e.message}"
                // Log the full exception for debugging
                // Log.e("AISuggestionViewModel", "Gemini API Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
