package com.fatokifury.meal_flow.navigation
import android.os.Build
import com.fatokifury.meal_flow.ui.viewmodels.AuthViewModel
import com.fatokifury.meal_flow.ui.viewmodels.AuthResultState
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fatokifury.meal_flow.ui.screens.LoginScreen
import com.fatokifury.meal_flow.ui.screens.SignUpScreen
import com.fatokifury.meal_flow.ui.screens.AddRecipeScreen // Import AddRecipeScreen
import com.fatokifury.meal_flow.ui.screens.RecipeListScreen
import com.fatokifury.meal_flow.ui.screens.RecipeDetailScreen // Import the new screen
import com.fatokifury.meal_flow.ui.screens.ImportRecipeScreen // Import the new ImportRecipeScreen
import com.fatokifury.meal_flow.ui.screens.MealCalendarScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


// Define Routes

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object SignUp : Screen("signup_screen")
    data object MealList : Screen("meal_list_screen")
    data object AISuggestions : Screen("ai_suggestions_screen")
    data object MealCalendar : Screen("meal_calendar_screen")
    data object AddRecipe : Screen(
        "add_recipe_screen" +
                "?title={title}" +
                "&imageUrl={imageUrl}" +
                "&description={description}" +
                "&ingredientsJson={ingredientsJson}" +
                "&stepsJson={stepsJson}" +
                "&tagsJson={tagsJson}"
    ) {
        fun createRoute(
            title: String? = null,
            imageUrl: String? = null,
            description: String? = null,
            ingredientsJson: String? = null,
            stepsJson: String? = null,
            tagsJson: String? = null
        ): String {
            var route = "add_recipe_screen"
            val params = mutableListOf<String>()

            // URL encode each parameter to be safe
            title?.let { params.add("title=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            imageUrl?.let { params.add("imageUrl=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            description?.let { params.add("description=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            ingredientsJson?.let { params.add("ingredientsJson=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            stepsJson?.let { params.add("stepsJson=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            tagsJson?.let { params.add("tagsJson=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }

            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            return route
        }
    }
    data object RecipeDetail : Screen("recipe_detail_screen/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail_screen/$recipeId"
    }
    data object ImportRecipe : Screen("import_recipe_screen?url={url}") { // Added ?url= to make it optional if we navigate to it without a url initially
        fun createRoute(encodedUrl: String) = "import_recipe_screen?url=$encodedUrl"
    }
}


@Composable
fun PlaceholderScreen(
    screenName: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    showButton: Boolean = false,
    modifier: Modifier = Modifier
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    startDestination: String = Screen.SignUp.route
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            // ... (LoginScreen composable remains the same)
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthResultState.Success -> {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.MealList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        authViewModel.resetAuthState()
                    }

                    is AuthResultState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetAuthState()
                    }

                    else -> Unit
                }
            }

            LoginScreen(
                onLoginClick = { email, pass ->
                    authViewModel.loginUser(email, pass)
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                isLoading = authState is AuthResultState.Loading
            )
        }

        composable(Screen.SignUp.route) {
            // ... (SignUpScreen composable remains the same)
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthResultState.Success -> {
                        Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.MealList.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                        authViewModel.resetAuthState()
                    }

                    is AuthResultState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        authViewModel.resetAuthState()
                    }

                    else -> Unit
                }
            }

            SignUpScreen(
                onSignUpClick = { fullName, email, pass, _ ->
                    authViewModel.signUpUser(fullName, email, pass)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                isLoading = authState is AuthResultState.Loading
            )
        }

        composable(Screen.MealList.route) {
            RecipeListScreen(navController = navController)
        }
        composable(Screen.AISuggestions.route) {
            PlaceholderScreen(screenName = "AI Meal Suggestions Screen")
        }
        composable(Screen.MealCalendar.route) {
            MealCalendarScreen(navController = navController)
        }

        // Updated composable for AddRecipeScreen
        composable(
            route = Screen.AddRecipe.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("imageUrl") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("description") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("ingredientsJson") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("stepsJson") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("tagsJson") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            // Retrieve arguments. AddRecipeScreen doesn't accept these yet,
            // but AddRecipeViewModel will be modified to use them.
            // val title = backStackEntry.arguments?.getString("title")
            // val imageUrl = backStackEntry.arguments?.getString("imageUrl")
            // val description = backStackEntry.arguments?.getString("description")
            // val ingredientsJson = backStackEntry.arguments?.getString("ingredientsJson")
            // val stepsJson = backStackEntry.arguments?.getString("stepsJson")
            // val tagsJson = backStackEntry.arguments?.getString("tagsJson")

            // For now, AddRecipeScreen is called without these args.
            // ViewModel will be responsible for picking them up from SavedStateHandle.
            AddRecipeScreen(navController = navController)
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            RecipeDetailScreen(navController = navController)
        }

        composable(
            route = Screen.ImportRecipe.route,
            arguments = listOf(navArgument("url") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url")
            ImportRecipeScreen(navController = navController, encodedUrl = encodedUrl)
        }
    }
}



