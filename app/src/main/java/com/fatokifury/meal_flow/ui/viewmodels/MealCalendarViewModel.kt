package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class MealCalendarUiState(
    val currentMonth: YearMonth,
    val selectedDate: LocalDate,
    val daysOfWeek: List<DayOfWeek> = emptyList(),
    val plannedMealsForSelectedDate: List<String> = emptyList(), // Added for planned meals
    val errorMessage: String? = null
)

@HiltViewModel
class MealCalendarViewModel @Inject constructor() : ViewModel() {

    private lateinit var _uiState: MutableStateFlow<MealCalendarUiState>
    val uiState: StateFlow<MealCalendarUiState>
        get() = _uiState.asStateFlow()

    // Mock data for planned meals
    private val mockPlannedMeals = mutableMapOf<LocalDate, List<String>>()

    init {
        Log.d("MealCalendarVM", "ViewModel initializing...")
        try {
            val initialCurrentMonth = YearMonth.now()
            val initialSelectedDate = LocalDate.now()

            // Populate some mock meals
            populateMockMeals(initialSelectedDate)

            val sortedDays = listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            )
            Log.d("MealCalendarVM", "Days of week prepared.")

            _uiState = MutableStateFlow(
                MealCalendarUiState(
                    currentMonth = initialCurrentMonth,
                    selectedDate = initialSelectedDate,
                    daysOfWeek = sortedDays,
                    plannedMealsForSelectedDate = mockPlannedMeals[initialSelectedDate] ?: emptyList() // Initialize with meals
                )
            )
            Log.i("MealCalendarVM", "ViewModel initialized successfully.")

        } catch (e: Throwable) {
            Log.e("MealCalendarVM", "CRITICAL ERROR during ViewModel initialization: ${e.message}", e)
            _uiState = MutableStateFlow( // Initialize _uiState in catch block
                MealCalendarUiState(
                    currentMonth = YearMonth.of(2024, 1), // Safe fallback
                    selectedDate = LocalDate.of(2024, 1, 1), // Safe fallback
                    daysOfWeek = listOf(DayOfWeek.SUNDAY), // Safe fallback
                    plannedMealsForSelectedDate = emptyList(), // Safe fallback
                    errorMessage = "Error initializing calendar: ${e.message}"
                )
            )
        }
    }

    // Helper function to populate mock meals
    private fun populateMockMeals(today: LocalDate) {
        mockPlannedMeals[today] = listOf("Spaghetti Bolognese", "Salad")
        mockPlannedMeals[today.plusDays(1)] = listOf("Chicken Stir-fry")
        mockPlannedMeals[today.minusDays(2)] = listOf("Oatmeal", "Fruit Smoothie", "Grilled Salmon")
        mockPlannedMeals[today.plusDays(3)] = listOf("Pizza Night!")
        // Add a few more for variety
        mockPlannedMeals[today.plusMonths(1).withDayOfMonth(5)] = listOf("Tacos")
        mockPlannedMeals[today.minusMonths(1).withDayOfMonth(15)] = listOf("Lentil Soup", "Bread")
    }


    fun onDateSelected(date: LocalDate) {
        if (!::_uiState.isInitialized) {
            Log.e("MealCalendarVM", "onDateSelected called but _uiState not initialized!")
            return
        }
        // For demonstration, let's add a new mock meal if one doesn't exist for a newly selected date
        if (!mockPlannedMeals.containsKey(date)) {
            val randomMealsBase = listOf("Avocado Toast", "Yogurt Parfait", "Scrambled Eggs", "Tuna Sandwich", "Leftover Pasta")
            mockPlannedMeals[date] = randomMealsBase.shuffled().take((0..2).random()) // Add 0 to 2 random meals
        }

        _uiState.update {
            it.copy(
                selectedDate = date,
                plannedMealsForSelectedDate = mockPlannedMeals[date] ?: emptyList()
            )
        }
        Log.d("MealCalendarVM", "Date selected: $date, Meals: ${mockPlannedMeals[date]}")
    }

    fun onMonthChanged(newMonth: YearMonth) {
        if (!::_uiState.isInitialized) {
            Log.e("MealCalendarVM", "onMonthChanged called but _uiState not initialized!")
            return
        }
        _uiState.update { it.copy(currentMonth = newMonth) }
    }

    fun navigateToPreviousMonth() {
        if (!::_uiState.isInitialized) {
            Log.e("MealCalendarVM", "navigateToPreviousMonth called but _uiState not initialized!")
            return
        }
        _uiState.update {
            val prevMonth = it.currentMonth.minusMonths(1)
            it.copy(currentMonth = prevMonth)
        }
    }

    fun navigateToNextMonth() {
        if (!::_uiState.isInitialized) {
            Log.e("MealCalendarVM", "navigateToNextMonth called but _uiState not initialized!")
            return
        }
        _uiState.update {
            val nextMonth = it.currentMonth.plusMonths(1)
            it.copy(currentMonth = nextMonth)
        }
    }

    fun getDayAbbreviation(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun clearErrorMessage() {
        if (!::_uiState.isInitialized) {
            Log.e("MealCalendarVM", "clearErrorMessage called but _uiState not initialized!")
            return
        }
        _uiState.update { it.copy(errorMessage = null) }
    }
}
