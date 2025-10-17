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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fatokifury.meal_flow.ui.screens.*
import com.fatokifury.meal_flow.ui.viewmodels.AppViewModel
import com.fatokifury.meal_flow.ui.viewmodels.AuthResultEvent
import com.fatokifury.meal_flow.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    // We get the viewmodels here
    appViewModel: AppViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 1. LISTEN to the startDestination from the AppViewModel
    val startDestination by appViewModel.startDestination.collectAsState()

    val navController: NavHostController = rememberNavController()

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

    // 2. CHECK if the destination is ready
    if (startDestination != null) {
        // 3. If it's ready, build the NavHost with the CORRECT startDestination
        NavHost(
            navController = navController,
            startDestination = startDestination!!, // Use the value from the ViewModel
            modifier = modifier
        ) {
            composable(Screen.Login.route) {
                LoginScreen(authViewModel = authViewModel)
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(authViewModel = authViewModel)
            }

            composable(Screen.MealList.route) {
                RecipeListScreen(onlogout = authViewModel::onLogoutClicked)
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
            ) {
                RecipeDetailScreen()
            }

            composable(
                route = Screen.ImportRecipe.route,
                arguments = listOf(navArgument("url") {
                    type = NavType.StringType; nullable = true
                })
            ) {
                ImportRecipeScreen()
            }
        }
    } else {
        // If the destination isn't ready yet, show a loading indicator
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
