package com.fatokifury.meal_flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fatokifury.meal_flow.navigation.AppNavHost
import com.fatokifury.meal_flow.navigation.Screen
import com.fatokifury.meal_flow.ui.theme.MealFlowTheme
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth // Declare FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAuth = FirebaseAuth.getInstance() // Initialize FirebaseAuth

        // Determine the start destination based on the current user
        val startDestination = if (firebaseAuth.currentUser != null) {
            Screen.MealList.route // User is logged in, go to MealList
        } else {
            Screen.SignUp.route // User is not logged in, go to SignUp (or Login)
        }

        setContent {
            MealFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination // Pass the dynamic startDestination
                    )
                }
            }
        }
    }
}
