// 4. Update AppNavHost.kt to use MainScaffold
// File: navigation/AppNavHost.kt

package com.fatokifury.meal_flow.navigation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    appViewModel: AppViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    // Determine if we should show bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in listOf(
        Screen.MealList.route,
        Screen.MealCalendar.route,
        Screen.Profile.route
    )

    if (startDestination != null) {
        if (showBottomNav) {
            // Screens WITH bottom navigation
            MainScaffold(navController = navController) { paddingModifier ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!,
                    modifier = paddingModifier
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

                    composable(Screen.Profile.route) {
                        ProfileScreen(onLogout = authViewModel::onLogoutClicked)
                    }



                    composable(
                        route = Screen.AddRecipe.route,
                        arguments = listOf(navArgument("recipeId") {
                            type = NavType.StringType; nullable = true
                        })
                    ) {
                        AddRecipeScreen(navController = navController)
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
            }
        } else {
            // Screens WITHOUT bottom nav (login, signup, detail screens, etc.)
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
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

                composable(Screen.Profile.route) {
                    ProfileScreen(onLogout = authViewModel::onLogoutClicked)
                }



                composable(
                    route = Screen.AddRecipe.route,
                    arguments = listOf(navArgument("recipeId") {
                        type = NavType.StringType; nullable = true
                    })
                ) {
                    AddRecipeScreen(navController = navController)
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
        }
    }
}