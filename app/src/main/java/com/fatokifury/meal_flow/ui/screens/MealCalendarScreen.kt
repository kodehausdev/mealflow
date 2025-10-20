package com.fatokifury.meal_flow.ui.screens

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.MealCalendarViewModel
import com.fatokifury.meal_flow.ui.viewmodels.MealItem
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

const val CALENDAR_SCREEN_TAG = "MealCalendarScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCalendarScreen(
    viewModel: MealCalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val currentMonth = uiState.currentMonth
    val firstDayOfWeekFromUiState = uiState.daysOfWeek.firstOrNull() ?: DayOfWeek.SUNDAY

    val startMonth = remember(currentMonth) { currentMonth.minusMonths(200) }
    val endMonth = remember(currentMonth) { currentMonth.plusMonths(200) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromUiState
    )

    LaunchedEffect(calendarState.firstVisibleMonth) {
        val visibleMonth = calendarState.firstVisibleMonth.yearMonth
        if (visibleMonth != uiState.currentMonth) {
            viewModel.onMonthChanged(visibleMonth)
        }
    }

    LaunchedEffect(uiState.currentMonth) {
        if (uiState.currentMonth != calendarState.firstVisibleMonth.yearMonth) {
            coroutineScope.launch {
                try {
                    calendarState.animateScrollToMonth(uiState.currentMonth)
                } catch (e: Exception) {
                    Log.e(CALENDAR_SCREEN_TAG, "Error scrolling: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = { ModernTopAppBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacing_medium)
        ) {
            ModernMonthNavigationHeader(
                currentMonth = uiState.currentMonth,
                onPreviousMonthClicked = { viewModel.navigateToPreviousMonth() },
                onNextMonthClicked = { viewModel.navigateToNextMonth() }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            ModernDaysOfWeekHeader(daysOfWeek = uiState.daysOfWeek)

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            HorizontalCalendar(
                state = calendarState,
                dayContent = { day ->
                    ModernDayView(
                        day = day,
                        isSelected = uiState.selectedDate == day.date,
                        isToday = day.date == LocalDate.now(),
                        hasMeals = viewModel.hasMealsForDate(day.date)
                    ) { clickedDay ->
                        if (clickedDay.position == DayPosition.MonthDate) {
                            viewModel.onDateSelected(clickedDay.date)
                        }
                    }
                },
                monthHeader = null
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(
                    id = R.string.meal_calendar_meals_for_date,
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = Dimens.spacing_small)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.plannedMealsForSelectedDate.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                ) {
                    items(
                        items = uiState.plannedMealsForSelectedDate,
                        key = { it.id }
                    ) { meal ->
                        MealCardItem(
                            meal = meal,
                            onDelete = { viewModel.removeMeal(uiState.selectedDate, meal.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = Dimens.spacing_medium)
                        )
                        Text(
                            text = stringResource(id = R.string.meal_calendar_no_meals_planned),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            Button(
                onClick = { viewModel.onAddMealClicked(uiState.selectedDate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(Dimens.spacing_medium)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = Dimens.spacing_small)
                )
                Text("Add Meal")
            }
        }
    }
}

@Composable
private fun MealCardItem(
    meal: MealItem,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "meal_scale"
    )
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) {},
        shape = RoundedCornerShape(Dimens.spacing_medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                if (meal.time != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_meal),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.meal_calendar_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ModernMonthNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonthClicked: () -> Unit,
    onNextMonthClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonthClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.previous_month),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonthClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next_month),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ModernDaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModernDayView(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    hasMeals: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    val date = day.date
    val baseModifier = if (day.position == DayPosition.MonthDate) {
        Modifier.clickable { onClick(day) }
    } else {
        Modifier
    }

    val selectionColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)

    Box(
        modifier = baseModifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> selectionColor
                    isToday -> todayColor
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onBackground
                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            },
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )

        if (hasMeals && day.position == DayPosition.MonthDate) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(6.dp)
                    .background(color = if (isSelected) MaterialTheme.colorScheme.onPrimary else selectionColor, shape = CircleShape)
            )
        }
    }
}
