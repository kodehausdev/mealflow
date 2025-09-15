package com.fatokifury.meal_flow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization // Added for description
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fatokifury.meal_flow.ui.viewmodels.AddRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navController: NavController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
            navController.popBackStack() // Go back after saving
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetSaveState() // Clear error after showing
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (uiState.title.isNotBlank()) "Edit Recipe" else "Add New Recipe") }) // Dynamic title
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveRecipe() },
                icon = { Icon(Icons.Filled.Add, "Save Recipe") },
                text = { Text("Save Recipe") },
                expanded = true
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Recipe Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Section - Added
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), // Make it a bit taller
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients Section
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            uiState.ingredients.forEach { ingredient ->
                ListItemWithRemove(text = ingredient, onRemove = { viewModel.removeIngredient(ingredient) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.currentIngredient,
                    onValueChange = viewModel::onCurrentIngredientChange,
                    label = { Text("Add Ingredient") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.addIngredient()
                        focusManager.clearFocus()
                    })
                )
                IconButton(onClick = {
                    viewModel.addIngredient()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Ingredient")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Steps Section
            Text("Steps", style = MaterialTheme.typography.titleMedium)
            uiState.steps.forEachIndexed { index, step ->
                ListItemWithRemove(text = "${index + 1}. $step", onRemove = { viewModel.removeStep(step) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.currentStep,
                    onValueChange = viewModel::onCurrentStepChange,
                    label = { Text("Add Step") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.addStep()
                        focusManager.clearFocus()
                    })
                )
                IconButton(onClick = {
                    viewModel.addStep()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Step")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Section (Optional)
            Text("Tags (Optional)", style = MaterialTheme.typography.titleMedium)
            uiState.tags.forEach { tag ->
                ListItemWithRemove(text = tag, onRemove = { viewModel.removeTag(tag) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.currentTag,
                    onValueChange = viewModel::onCurrentTagChange,
                    label = { Text("Add Tag") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.addTag()
                        focusManager.clearFocus()
                    })
                )
                IconButton(onClick = {
                    viewModel.addTag()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Tag")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Text("Recipe Image (Coming Soon!)", style = MaterialTheme.typography.titleMedium)


            if (uiState.isSaving) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ListItemWithRemove(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove")
        }
    }
}
