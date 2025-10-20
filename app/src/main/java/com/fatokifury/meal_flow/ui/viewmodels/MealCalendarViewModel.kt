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
import java.util.UUID
import javax.inject.Inject

data class MealItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val time: String? = null,
    val calories: Int? = null,
    val notes: String? = null
)

data class MealCalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysOfWeek: List<DayOfWeek> = emptyList(),
    val plannedMealsForSelectedDate: List<MealItem> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MealCalendarViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MealCalendarUiState())
    val uiState: StateFlow<MealCalendarUiState> = _uiState.asStateFlow()

    private val mockPlannedMeals = mutableMapOf<LocalDate, MutableList<MealItem>>()

    init {
        Log.d("MealCalendarVM", "ViewModel initializing...")
        try {
            val initialSelectedDate = LocalDate.now()
            populateMockMeals(initialSelectedDate)

            val sortedDays = listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            )

            _uiState.update {
                it.copy(
                    daysOfWeek = sortedDays,
                    plannedMealsForSelectedDate = mockPlannedMeals[initialSelectedDate]?.toList() ?: emptyList(),
                    isLoading = false
                )
            }
            Log.i("MealCalendarVM", "ViewModel initialized successfully.")
        } catch (e: Throwable) {
            Log.e("MealCalendarVM", "CRITICAL ERROR during ViewModel initialization: ${e.message}", e)
            _uiState.update {
                it.copy(
                    errorMessage = "Error initializing calendar: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun populateMockMeals(today: LocalDate) {
        mockPlannedMeals[today] = mutableListOf(
            MealItem("1", "Spaghetti Bolognese", "7:30 PM", 650, "Classic Italian dinner"),
            MealItem("2", "Caesar Salad", "7:45 PM", 320)
        )
        mockPlannedMeals[today.plusDays(1)] = mutableListOf(
            MealItem("3", "Chicken Stir-fry", "6:00 PM", 520, "With vegetables")
        )
        mockPlannedMeals[today.minusDays(2)] = mutableListOf(
            MealItem("4", "Oatmeal", "8:00 AM", 180, "With berries"),
            MealItem("5", "Fruit Smoothie", "10:00 AM", 250),
            MealItem("6", "Grilled Salmon", "7:00 PM", 480)
        )
        mockPlannedMeals[today.plusDays(3)] = mutableListOf(
            MealItem("7", "Pizza Night!", "6:30 PM", 600)
        )
        mockPlannedMeals[today.plusMonths(1).withDayOfMonth(5)] = mutableListOf(
            MealItem("8", "Tacos", "5:30 PM", 450)
        )
        mockPlannedMeals[today.minusMonths(1).withDayOfMonth(15)] = mutableListOf(
            MealItem("9", "Lentil Soup", "12:00 PM", 280),
            MealItem("10", "Whole Wheat Bread", "12:15 PM", 200)
        )
    }

    fun onDateSelected(date: LocalDate) {
        if (!mockPlannedMeals.containsKey(date)) {
            mockPlannedMeals[date] = mutableListOf()
        }

        _uiState.update {
            it.copy(
                selectedDate = date,
                plannedMealsForSelectedDate = mockPlannedMeals[date]?.toList() ?: emptyList()
            )
        }
        Log.d("MealCalendarVM", "Date selected: $date, Meals: ${mockPlannedMeals[date]?.size ?: 0}")
    }

    fun onMonthChanged(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
    }

    fun navigateToPreviousMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.minusMonths(1))
        }
    }

    fun navigateToNextMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
    }

    fun addMeal(date: LocalDate, meal: MealItem) {
        if (!mockPlannedMeals.containsKey(date)) {
            mockPlannedMeals[date] = mutableListOf()
        }

        mockPlannedMeals[date]?.add(meal)

        if (_uiState.value.selectedDate == date) {
            _uiState.update {
                it.copy(plannedMealsForSelectedDate = mockPlannedMeals[date]?.toList() ?: emptyList())
            }
        }

        Log.d("MealCalendarVM", "Meal added: ${meal.name} on $date")
    }

    fun removeMeal(date: LocalDate, mealId: String) {
        mockPlannedMeals[date]?.removeAll { it.id == mealId }

        if (_uiState.value.selectedDate == date) {
            _uiState.update {
                it.copy(plannedMealsForSelectedDate = mockPlannedMeals[date]?.toList() ?: emptyList())
            }
        }

        Log.d("MealCalendarVM", "Meal removed: $mealId from $date")
    }

    fun hasMealsForDate(date: LocalDate): Boolean {
        return (mockPlannedMeals[date]?.size ?: 0) > 0
    }

    fun onAddMealClicked(date: LocalDate) {
        Log.d("MealCalendarVM", "Add meal clicked for $date")
        val newMeal = MealItem(name = "New Sample Meal", time = "12:00 PM")
        addMeal(date, newMeal)
    }

    fun getDayAbbreviation(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
