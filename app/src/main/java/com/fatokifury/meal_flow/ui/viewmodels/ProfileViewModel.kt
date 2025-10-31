package com.fatokifury.meal_flow.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val navigationService: NavigationService
) : ViewModel() {



    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // We can add a state to hold the refreshed user if needed, or just trigger the refresh.
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    // --- THIS IS THE NEW, CRUCIAL PART ---
    init {
        // When the ViewModel is created, immediately ask Firebase to refresh the user data.
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // The reload was successful. Update our state with the fresh user object.
                _user.value = auth.currentUser
                Log.d("ProfileViewModel", "User data reloaded successfully.")
            } else {
                // Handle the failure, though it's rare.
                Log.e("ProfileViewModel", "Failed to reload user data.", task.exception)
            }
        }
    }





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
        } catch (_: Exception) {
            // If Play Store app not available, open in browser
            val intent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri())
            context.startActivity(intent)
        }
    }
}