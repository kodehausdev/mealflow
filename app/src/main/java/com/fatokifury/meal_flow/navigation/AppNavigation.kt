package com.fatokifury.meal_flow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier



sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object SignUp : Screen("signup_screen")
    data object MealList : Screen("meal_list_screen")
    data object AISuggestions : Screen("ai_suggestions_screen")
    data object MealCalendar : Screen("meal_calendar_screen")

    data object AddRecipe : Screen("add_recipe_screen?recipeId={recipeId}") {
        fun createRoute(recipeId: String?): String {
            return "add_recipe_screen?recipeId=${recipeId ?: "null"}"
        }
    }

    data object RecipeDetail : Screen("recipe_detail_screen/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail_screen/$recipeId"
    }

    data object ImportRecipe : Screen("import_recipe_screen?url={url}") {
        fun createRoute(encodedUrl: String) = "import_recipe_screen?url=$encodedUrl"
    }
}

@Composable
fun PlaceholderScreen(
    modifier: Modifier = Modifier,
    screenName: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    showButton: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome to $screenName")
            if (showButton && buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onButtonClick) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

