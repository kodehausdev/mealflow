package com.fatokifury.meal_flow.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val navigationService: NavigationService
) : ViewModel() {

    fun onImportRecipeClick() {
        // Navigate to ImportRecipe screen with empty URL
        navigationService.navigate(Screen.ImportRecipe.createRoute(""))
    }

    fun onSettingsClick() {
        // TODO: Implement settings screen when ready
        // navigationService.navigate(Screen.Settings.route)
    }

    fun shareApp(context: Context) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT,
                "Check out this amazing recipe app! [Add Play Store link here]")
            putExtra(Intent.EXTRA_SUBJECT, "MealFlow - Recipe App")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share MealFlow"))
    }

    fun rateApp(context: Context) {
        val packageName = context.packageName
        try {
            // Try to open in Play Store app
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            // If Play Store app not available, open in browser
            val intent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri())
            context.startActivity(intent)
        }
    }
}