package com.fatokifury.meal_flow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// region Enhanced MealFlow Color Palette - More Vibrant & Elevated

// --- Primary (Coral/Salmon Pink) - More saturated and vibrant ---
val primaryLight = Color(0xFFFF6B6B) // More vibrant coral
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFE0E0) // Lighter, more elevated
val onPrimaryContainerLight = Color(0xFF410000)

// --- Secondary (Deep Blue) - Richer, more premium ---
val secondaryLight = Color(0xFF4A5FC1) // Richer blue
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE0E5FF) // Lighter container
val onSecondaryContainerLight = Color(0xFF001456)

// --- Tertiary (Vibrant Red) - More punchy ---
val tertiaryLight = Color(0xFFE63946) // Brighter red
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFE0E0)
val onTertiaryContainerLight = Color(0xFF410000)

// --- Neutrals - Cleaner whites and grays ---
val errorLight = Color(0xFFDC3545)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFE5E8)
val onErrorContainerLight = Color(0xFF5C0000)
val backgroundLight = Color(0xFFFFFBFA) // Warmer, cleaner white
val onBackgroundLight = Color(0xFF1A1110)
val surfaceLight = Color(0xFFFFFFFF) // Pure white for cards
val onSurfaceLight = Color(0xFF1A1110)
val surfaceVariantLight = Color(0xFFFFF0EE) // Subtle peachy tint
val onSurfaceVariantLight = Color(0xFF524341)
val outlineLight = Color(0xFFBAABA8)
val outlineVariantLight = Color(0xFFE8DFDD)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF352F2E)
val inverseOnSurfaceLight = Color(0xFFFFF0EE)
val inversePrimaryLight = Color(0xFFFFB4AB)

// Container colors for elevated surfaces
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF8F7)
val surfaceContainerLight = Color(0xFFFFF0EE)
val surfaceContainerHighLight = Color(0xFFFCEAE7)
val surfaceContainerHighestLight = Color(0xFFF7E4E1)

// --- Dark Theme Colors - Deeper, more dramatic ---
val primaryDark = Color(0xFFFF8A80) // Brighter in dark mode
val onPrimaryDark = Color(0xFF5C0016)
val primaryContainerDark = Color(0xFF7D1F2A)
val onPrimaryContainerDark = Color(0xFFFFD9DC)

val secondaryDark = Color(0xFF9BA5FF) // Brighter blue
val onSecondaryDark = Color(0xFF001E68)
val secondaryContainerDark = Color(0xFF2E3F8F)
val onSecondaryContainerDark = Color(0xFFDDE1FF)

val tertiaryDark = Color(0xFFFFB3B0) // Softer in dark
val onTertiaryDark = Color(0xFF680009)
val tertiaryContainerDark = Color(0xFF930014)
val onTertiaryContainerDark = Color(0xFFFFDAD8)

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF1A1110) // Deep warm black
val onBackgroundDark = Color(0xFFF5E4E2)
val surfaceDark = Color(0xFF1F1B1A) // Elevated surface
val onSurfaceDark = Color(0xFFF5E4E2)
val surfaceVariantDark = Color(0xFF534341)
val onSurfaceVariantDark = Color(0xFFD9C2BE)
val outlineDark = Color(0xFF9F8D89)
val outlineVariantDark = Color(0xFF534341)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF5E4E2)
val inverseOnSurfaceDark = Color(0xFF352F2E)
val inversePrimaryDark = Color(0xFFB92F3E)

// Container colors for elevated surfaces in dark
val surfaceContainerLowestDark = Color(0xFF120D0C)
val surfaceContainerLowDark = Color(0xFF221917)
val surfaceContainerDark = Color(0xFF271D1C)
val surfaceContainerHighDark = Color(0xFF322726)
val surfaceContainerHighestDark = Color(0xFF3D3231)

// endregion


val AppLightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inverseSurface = inverseSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceTint = primaryLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    // Enhanced container surfaces
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val AppDarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inverseSurface = inverseSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceTint = primaryDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    // Enhanced container surfaces
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)


// Custom colors for key UI elements (work in both light and dark)
object AppColors {
    // FAB colors
    val FabContainerLight = Color(0xFFD32F2F) // Vibrant red
    val FabContainerDark = Color(0xFFEF5350)  // Lighter red for dark mode
    val FabContentLight = Color.White
    val FabContentDark = Color.White

    // Bottom Nav
    val BottomNavSelectedLight = Color(0xFF1976D2) // Blue
    val BottomNavSelectedDark = Color(0xFF42A5F5)  // Lighter blue
    val BottomNavIndicatorLight = Color(0xFFBBDEFB)
    val BottomNavIndicatorDark = Color(0xFF1565C0)

    // Import Button
    val ImportButtonLight = Color(0xFFE91E63) // Pink
    val ImportButtonDark = Color(0xFFF48FB1)  // Lighter pink
    val ImportContentLight = Color.White
    val ImportContentDark = Color(0xFF880E4F)  // Dark pink

    // Search Bar
    val SearchBarLight = Color(0xFFF5F5F5)
    val SearchBarDark = Color(0xFF2C2C2C)
}