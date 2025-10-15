package com.fatokifury.meal_flow.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController // <-- Import if missing
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController // <-- Import if missing
import androidx.navigation.navArgument
import com.fatokifury.meal_flow.ui.screens.*
import com.fatokifury.meal_flow.ui.viewmodels.AppViewModel
import com.fatokifury.meal_flow.ui.viewmodels.AuthResultEvent
import com.fatokifury.meal_flow.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
//    val startDestination by appViewModel.startDestination.collectAsState()

    // 1. Create the NavController here, where it belongs.
    val navController: NavHostController = rememberNavController()

    // 2. Use a LaunchedEffect to give the NavController to the service.
    // This connects your navigation graph to the service that triggers navigation.
    LaunchedEffect(Unit) {
        appViewModel.navigationService.setNavController(navController)
    }

    LaunchedEffect(Unit) {
        authViewModel.authEvents.collectLatest { event ->
            when (event) {
                is AuthResultEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is AuthResultEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 3. Use the locally created navController for the NavHost.
//    if (startDestination == null) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(authViewModel = authViewModel)
        }

        composable(Screen.MealList.route) {
            RecipeListScreen(onlogout = authViewModel :: onLogoutClicked)
        }
        composable(Screen.MealCalendar.route) {
            MealCalendarScreen()
        }

        composable(
            route = Screen.AddRecipe.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType; nullable = true
            })
        ) {
            AddRecipeScreen()
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            RecipeDetailScreen()
        }

        composable(
            route = Screen.ImportRecipe.route,
            arguments = listOf(navArgument("url") {
                type = NavType.StringType; nullable = true
            })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url")
            ImportRecipeScreen()
        }
    }
}