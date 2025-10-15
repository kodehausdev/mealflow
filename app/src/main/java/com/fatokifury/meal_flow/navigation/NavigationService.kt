package com.fatokifury.meal_flow.navigation

import androidx.navigation.NavHostController
import javax.inject.Inject
import javax.inject.Singleton

interface NavigationService {
    fun navigate(route: String)
    fun navigateAndPopUp(route: String, popUpTo: String)
    fun goBack()
    fun setNavController(navController: NavHostController)
}

@Singleton
class NavigationServiceImpl @Inject constructor() : NavigationService {
    private lateinit var navController: NavHostController

    override fun navigate(route: String) {
        if (::navController.isInitialized) {
            navController.navigate(route)
        }
    }

    override fun navigateAndPopUp(route: String, popUpTo: String) {
        if (::navController.isInitialized) {
            navController.navigate(route) {
                popUpTo(popUpTo) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    override fun goBack() {
        if (::navController.isInitialized) {
            navController.popBackStack()
        }
    }

    override fun setNavController(navController: NavHostController) {
        this.navController = navController
    }
}
