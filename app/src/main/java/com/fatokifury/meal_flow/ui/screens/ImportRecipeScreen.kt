package com.fatokifury.meal_flow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fatokifury.meal_flow.navigation.Screen // Added
import com.fatokifury.meal_flow.ui.viewmodels.ImportRecipeViewModel
import com.fatokifury.meal_flow.ui.viewmodels.ParsedRecipeArgs // Added
import kotlinx.serialization.builtins.ListSerializer // Added for JSON
import kotlinx.serialization.builtins.serializer // Added for JSON
import kotlinx.serialization.json.Json // Added for JSON
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRecipeScreen(
    navController: NavController,
    encodedUrl: String?,
    viewModel: ImportRecipeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val decodedUrl = remember(encodedUrl) {
        try {
            encodedUrl?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(decodedUrl) {
        if (decodedUrl != null) {
            viewModel.startImport(decodedUrl)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    // Configure Json for serializing lists
    val json = remember { Json { ignoreUnknownKeys = true; isLenient = true } }

    LaunchedEffect(uiState.successNavigationArgs) {
        uiState.successNavigationArgs?.let { args: ParsedRecipeArgs ->
            try {
                val ingredientsJson = args.ingredients?.let { json.encodeToString(ListSerializer(String.serializer()), it) }
                val stepsJson = args.steps?.let { json.encodeToString(ListSerializer(String.serializer()), it) }
                val tagsJson = args.tags?.let { json.encodeToString(ListSerializer(String.serializer()), it) }

                val route = Screen.AddRecipe.createRoute(
                    title = args.title,
                    imageUrl = args.imageUrl,
                    description = args.description,
                    ingredientsJson = ingredientsJson,
                    stepsJson = stepsJson,
                    tagsJson = tagsJson
                )
                navController.navigate(route) {
                    // Optional: popUpTo ImportRecipeScreen if you want it removed from backstack
                    popUpTo(Screen.ImportRecipe.route) { inclusive = true }
                }
            } catch (e: Exception) {
                // Handle JSON encoding error, though unlikely for List<String>
                Toast.makeText(context, "Error preparing recipe data for editing.", Toast.LENGTH_LONG).show()
            } finally {
                viewModel.navigationHandled() // Signal that navigation has been handled or attempted
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (uiState.isLoading) "Importing..." else "Import Recipe") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Processing URL:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(uiState.importUrl ?: "No URL")
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Import Failed")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.errorMessage ?: "An unknown error occurred.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                uiState.successNavigationArgs != null -> {
                    Text("Import successful! Preparing recipe...")
                }
                decodedUrl == null && !uiState.isLoading -> {
                    Text("Invalid or missing URL provided.")
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ready to import from:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(decodedUrl ?: "No URL provided")
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

