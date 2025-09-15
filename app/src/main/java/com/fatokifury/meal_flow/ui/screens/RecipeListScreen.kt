package com.fatokifury.meal_flow.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.Screen
import com.fatokifury.meal_flow.ui.viewmodels.RecipeListViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.KITKAT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    navController: NavController,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf(TextFieldValue("")) }

    // Handle error messages - check if your UiState has errorMessage property
    LaunchedEffect(uiState) {
        // Replace 'errorMessage' with the actual property name in your UiState
        val errorMessage = when {
            // Check if your UiState has an errorMessage property
            uiState::class.java.declaredFields.any { it.name == "errorMessage" } -> {
                try {
                    val field = uiState::class.java.getDeclaredField("errorMessage")
                    field.isAccessible = true
                    field.get(uiState) as? String
                } catch (e: Exception) { null }
            }
            // Check if your UiState has an error property
            uiState::class.java.declaredFields.any { it.name == "error" } -> {
                try {
                    val field = uiState::class.java.getDeclaredField("error")
                    field.isAccessible = true
                    field.get(uiState) as? String
                } catch (e: Exception) { null }
            }
            else -> null
        }

        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            // Call the appropriate clear method if it exists
            try {
                val clearMethod = viewModel::class.java.getMethod("clearErrorMessage")
                clearMethod.invoke(viewModel)
            } catch (e: Exception) {
                // Method doesn't exist, ignore
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") },
                actions = {
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = "Import Recipe from URL"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddRecipe.route) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add New Recipe"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Get loading state
            val isLoading = try {
                val field = uiState::class.java.getDeclaredField("isLoading")
                field.isAccessible = true
                field.get(uiState) as? Boolean ?: false
            } catch (e: Exception) { false }

            // Get recipes list
            val recipes = try {
                val field = uiState::class.java.getDeclaredField("recipes")
                field.isAccessible = true
                field.get(uiState) as? List<Recipe> ?: emptyList()
            } catch (e: Exception) { emptyList<Recipe>() }

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                recipes.isEmpty() -> {
                    Text(
                        text = "No recipes yet. Tap the '+' button to add your first recipe or use the link icon to import from a URL!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recipes, key = { recipe -> recipe.id }) { recipe ->
                            val dismissBoxState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.StartToEnd,
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            viewModel.deleteRecipe(recipe)
                                            true
                                        }
                                        SwipeToDismissBoxValue.Settled -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissBoxState,
                                modifier = Modifier.padding(vertical = 4.dp),
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = when (dismissBoxState.targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        },
                                        label = "dismiss bg color"
                                    )

                                    val alignment = when (dismissBoxState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                                    }

                                    val scale by animateFloatAsState(
                                        targetValue = if (dismissBoxState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                                        label = "dismiss icon scale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Icon",
                                            tint = Color.White,
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                }
                            ) {
                                RecipeListItem(
                                    recipe = recipe,
                                    onClick = {
                                        navController.navigate(Screen.RecipeDetail.createRoute(recipe.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Import URL Dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Import Recipe from URL") },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Enter recipe URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val url = urlInput.text.trim()
                        if (url.isNotBlank()) {
                            try {
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.ImportRecipe.createRoute(encodedUrl))
                                showUrlDialog = false
                                urlInput = TextFieldValue("")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid URL format", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "URL cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showUrlDialog = false
                    urlInput = TextFieldValue("")
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium
            )

            if (!recipe.description.isNullOrBlank()) {
                Text(
                    text = recipe.description.take(100) +
                            if (recipe.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}