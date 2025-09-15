package com.fatokifury.meal_flow.ui.theme

// Import new Vibe A colors
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// Updated DarkColorScheme for Vibe A
private val DarkColorScheme = darkColorScheme(
    primary = DarkNavy,
    onPrimary = OffWhite,
    primaryContainer = DarkSurface,
    onPrimaryContainer = LightSteelBlue,
    inversePrimary = ElectricBlue, // Or a lighter variant of DarkNavy for contrast

    secondary = ElectricBlue,
    onSecondary = DarkNavy, // Ensuring contrast on bright blue
    secondaryContainer = DeepOcean,
    onSecondaryContainer = OffWhite,

    tertiary = LightSteelBlue,
    onTertiary = DarkNavy,
    tertiaryContainer = MutedSteelBlue,
    onTertiaryContainer = OffWhite,

    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorRedContainer,
    onErrorContainer = OnErrorRedContainer,

    background = DarkNavy,
    onBackground = OffWhite,

    surface = DarkSurface,
    onSurface = OffWhite,

    surfaceVariant = DarkerNavy,
    onSurfaceVariant = LightSteelBlue,

    outline = LightSteelBlue,
    outlineVariant = DarkSurface, //  A less prominent outline

    surfaceTint = DarkNavy // Typically same as primary in dark themes
)

// LightColorScheme remains as default for now, can be updated later if needed
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override if needed for a custom light theme:
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MealFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disabling dynamic color to enforce our custom Vibe A theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Consider creating a Vibe A LightColorScheme later
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Adjusted for dark theme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography.kt exists and is set up
        shapes = Shapes,         // Assuming Shapes.kt exists and is set up
        content = content
    )
}
