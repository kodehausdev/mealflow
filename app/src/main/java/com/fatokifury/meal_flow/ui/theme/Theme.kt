package com.fatokifury.meal_flow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable


// ... imports
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Make sure your LightColorScheme and DarkColorScheme are imported
// ...

@Composable
fun MealFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // This is the key!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Your existing typography
        content = content
    )
}

// Add this to the bottom of your ui/theme/Theme.kt file

@Preview(name = "Light Theme Preview", showBackground = true)
@Composable
fun LightThemePreview() {
    // We explicitly pass darkTheme = false to see the light theme
    MealFlowTheme(darkTheme = false) {
        ThemeSurface()
    }
}

@Preview(name = "Dark Theme Preview", showBackground = true)
@Composable
fun DarkThemePreview() {
    // We explicitly pass darkTheme = true to see the dark theme
    MealFlowTheme(darkTheme = true) {
        ThemeSurface()
    }
}

@Composable
fun ThemeSurface() {
    // Use a surface to see the background/surface colors correctly
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Primary Text",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary // Shows primary color
            )
            Text(
                text = "Body text on surface",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface // Shows text color on surface
            )
            Button(onClick = { /*TODO*/ }) {
                Text("Primary Button")
            }
            OutlinedButton(onClick = { /*TODO*/ }) {
                Text("Outlined Button")
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant // Shows a card
                )
            ) {
                Text(
                    text = "Text on Surface Variant",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


