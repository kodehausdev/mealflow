package com.fatokifury.meal_flow.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Added for meal list
import androidx.compose.foundation.lazy.items     // Added for meal list
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fatokifury.meal_flow.ui.viewmodels.MealCalendarViewModel
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
    navController: NavController,
    viewModel: MealCalendarViewModel = hiltViewModel()
) {
    Log.d(CALENDAR_SCREEN_TAG, "Composable rendering...")

    val uiState by viewModel.uiState.collectAsState()
    Log.d(CALENDAR_SCREEN_TAG, "uiState collected: currentMonth=${uiState.currentMonth}, selectedDate=${uiState.selectedDate}, meals=${uiState.plannedMealsForSelectedDate.size}")

    val currentMonth = uiState.currentMonth
    // val selectedDate = uiState.selectedDate // Use uiState.selectedDate directly

    val coroutineScope = rememberCoroutineScope()
    Log.d(CALENDAR_SCREEN_TAG, "CoroutineScope remembered.")

    val firstDayOfWeekFromUiState = uiState.daysOfWeek.firstOrNull() ?: DayOfWeek.SUNDAY
    Log.d(CALENDAR_SCREEN_TAG, "First day of week from UI state: $firstDayOfWeekFromUiState")

    val startMonth = remember(currentMonth) {
        Log.d(CALENDAR_SCREEN_TAG, "Remembering startMonth, based on currentMonth: $currentMonth")
        currentMonth.minusMonths(200).also {
            Log.d(CALENDAR_SCREEN_TAG, "Calculated startMonth: $it")
        }
    }
    val endMonth = remember(currentMonth) {
        Log.d(CALENDAR_SCREEN_TAG, "Remembering endMonth, based on currentMonth: $currentMonth")
        currentMonth.plusMonths(200).also {
            Log.d(CALENDAR_SCREEN_TAG, "Calculated endMonth: $it")
        }
    }

    Log.d(CALENDAR_SCREEN_TAG, "Preparing calendarState: start=$startMonth, end=$endMonth, firstVisible=$currentMonth, firstDayOfWeek=$firstDayOfWeekFromUiState")
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromUiState
    )
    Log.d(CALENDAR_SCREEN_TAG, "calendarState created. First visible month: ${calendarState.firstVisibleMonth.yearMonth}")

    LaunchedEffect(calendarState.firstVisibleMonth) {
        Log.d(CALENDAR_SCREEN_TAG, "LaunchedEffect for calendarState.firstVisibleMonth triggered. Visible: ${calendarState.firstVisibleMonth.yearMonth}, ViewModel's: ${uiState.currentMonth}")
        val visibleMonth = calendarState.firstVisibleMonth.yearMonth
        if (visibleMonth != uiState.currentMonth) {
            Log.d(CALENDAR_SCREEN_TAG, "Visible month changed by scroll, updating ViewModel to: $visibleMonth")
            viewModel.onMonthChanged(visibleMonth)
        }
    }

    LaunchedEffect(uiState.currentMonth) {
        Log.d(CALENDAR_SCREEN_TAG, "LaunchedEffect for uiState.currentMonth triggered. ViewModel's: ${uiState.currentMonth}, Calendar's: ${calendarState.firstVisibleMonth.yearMonth}")
        if (uiState.currentMonth != calendarState.firstVisibleMonth.yearMonth) {
            Log.d(CALENDAR_SCREEN_TAG, "ViewModel month changed, scrolling calendar to: ${uiState.currentMonth}")
            coroutineScope.launch {
                try {
                    calendarState.animateScrollToMonth(uiState.currentMonth)
                    Log.d(CALENDAR_SCREEN_TAG, "animateScrollToMonth completed for ${uiState.currentMonth}")
                } catch (e: Exception) {
                    Log.e(CALENDAR_SCREEN_TAG, "Error in animateScrollToMonth: ${e.message}", e)
                }
            }
        }
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1), // Indigo
                Color(0xFF8B5CF6), // Violet
                Color(0xFFA855F7), // Purple
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }

    Log.d(CALENDAR_SCREEN_TAG, "Rendering Scaffold...")
    Scaffold(
        topBar = {
            Log.d(CALENDAR_SCREEN_TAG, "Rendering TopAppBar...")
            ModernTopAppBar() // Using your existing ModernTopAppBar
            Log.d(CALENDAR_SCREEN_TAG, "TopAppBar rendered.")
        },
        containerColor = Color.Transparent // For the gradient background to show through
    ) { paddingValues ->
        Log.d(CALENDAR_SCREEN_TAG, "Scaffold content lambda. Padding: $paddingValues")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush) // Apply gradient to the whole screen box
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply scaffold padding
                    .padding(16.dp), // Overall padding for the card content
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f) // MODIFIED: More transparent main card
                ),
                elevation = CardDefaults.cardElevation(0.dp) // No shadow for this glass card style
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp) // Inner padding for card content
                ) {
                    Log.d(CALENDAR_SCREEN_TAG, "Rendering MonthNavigationHeader...")
                    ModernMonthNavigationHeader(
                        currentMonth = uiState.currentMonth,
                        onPreviousMonthClicked = { viewModel.navigateToPreviousMonth() },
                        onNextMonthClicked = { viewModel.navigateToNextMonth() }
                    )
                    Log.d(CALENDAR_SCREEN_TAG, "MonthNavigationHeader rendered.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Log.d(CALENDAR_SCREEN_TAG, "Rendering DaysOfWeekHeader...")
                    ModernDaysOfWeekHeader(daysOfWeek = uiState.daysOfWeek)
                    Log.d(CALENDAR_SCREEN_TAG, "DaysOfWeekHeader rendered.")

                    Spacer(modifier = Modifier.height(8.dp))

                    Log.d(CALENDAR_SCREEN_TAG, "Rendering HorizontalCalendar...")
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            ModernDayView(
                                day = day,
                                isSelected = uiState.selectedDate == day.date, // Use selectedDate from uiState
                                isToday = day.date == LocalDate.now(),
                                hasMeals = uiState.plannedMealsForSelectedDate.isNotEmpty() && uiState.selectedDate == day.date // Basic meal indicator for selected day
                            ) { clickedDay ->
                                Log.d(CALENDAR_SCREEN_TAG, "Day clicked: ${clickedDay.date}, position: ${clickedDay.position}")
                                if (clickedDay.position == DayPosition.MonthDate) {
                                    viewModel.onDateSelected(clickedDay.date)
                                }
                            }
                        },
                        monthHeader = null
                    )
                    Log.d(CALENDAR_SCREEN_TAG, "HorizontalCalendar rendered.")

                    Spacer(modifier = Modifier.height(24.dp))

                    // Display selected date and planned meals more elegantly
                    Text(
                        text = "Meals for: ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (uiState.plannedMealsForSelectedDate.isNotEmpty()) {
                        Log.d(CALENDAR_SCREEN_TAG, "Displaying ${uiState.plannedMealsForSelectedDate.size} meals.")
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Takes remaining space
                        ) {
                            items(uiState.plannedMealsForSelectedDate) { mealName ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.12f) // MODIFIED: More transparent meal items
                                    )
                                ) {
                                    Text(
                                        text = mealName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Log.d(CALENDAR_SCREEN_TAG, "No meals planned for selected date.")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f), // Takes remaining space
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No meals planned for this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    // Removed ModernSelectedDateCard as its content is now integrated above
                }
            }
        }
        Log.d(CALENDAR_SCREEN_TAG, "Scaffold content lambda finished.")
    }
    Log.d(CALENDAR_SCREEN_TAG, "Composable rendering finished.")
}

// ModernTopAppBar, ModernMonthNavigationHeader, ModernNavigationButton, ModernDaysOfWeekHeader, ModernDayView,
// Ensure they are correctly defined in your file.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Meal Calendar",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun ModernMonthNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonthClicked: () -> Unit,
    onNextMonthClicked: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ModernNavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            onClick = {
                isAnimating = true
                onPreviousMonthClicked()
            }
        )

        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialOffsetX = { if (targetState > initialState) 300 else -300 }
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            targetOffsetX = { if (targetState > initialState) -300 else 300 }
                        ) + fadeOut()
            },
            label = "month_transition"
        ) { month ->
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        ModernNavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            onClick = {
                isAnimating = true
                onNextMonthClicked()
            }
        )
    }
}

@Composable
fun ModernNavigationButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Color.White.copy(alpha = 0.2f),
                CircleShape
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.3f),
                CircleShape
            )
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
fun ModernDaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // Adjusted padding from previous version for consistency
    ) {
        if (daysOfWeek.isEmpty()) {
            Log.w(CALENDAR_SCREEN_TAG, "daysOfWeek is empty in DaysOfWeekHeader!")
        }
        for (dayOfWeek in daysOfWeek) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ModernDayView(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    hasMeals: Boolean, // Added to indicate if meals are present
    onClick: (CalendarDay) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "day_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White // Selected day background
            isToday -> Color.White.copy(alpha = 0.3f) // Today background
            day.position == DayPosition.MonthDate -> Color.Transparent
            else -> Color.Transparent
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "day_background"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFF6366F1) // Selected day text color (Indigo)
            day.position == DayPosition.MonthDate -> Color.White
            else -> Color.White.copy(alpha = 0.4f)
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "day_text_color"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday && !isSelected) 2.dp else 0.dp,
                color = if (isToday && !isSelected) Color.White.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = {
                    isPressed = true
                    onClick(day)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )

            // Meal indicator dot (more prominent if selected)
            if (hasMeals && day.position == DayPosition.MonthDate) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 6.dp else 4.dp) // Larger dot if selected
                        .background(
                            if (isSelected) Color(0xFF6366F1) else Color.White, // Match text color or white
                            CircleShape
                        )
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

// ModernSelectedDateCard is removed as its functionality is integrated into the main screen Column.
//
//@Preview(showBackground = true, name = "Meal Calendar Screen Preview")
//@Composable
//fun MealCalendarScreenPreview() {
//    // Consider wrapping with your app's theme if you have one
//    // com.fatokifury.meal_flow.ui.theme.MealFlowTheme {
//    MealCalendarScreen(
//        navController = androidx.navigation.compose.rememberNavController()
//        // ViewModel will be provided by hiltViewModel default in the actual composable
//    )
    // }
