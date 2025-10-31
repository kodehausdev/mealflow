package com.fatokifury.meal_flow.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.model.Ingredient
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.AddRecipeViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    navController: NavController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { scrollValue ->
                isFabVisible = scrollValue == 0
            }
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.recipeId != null)
                            stringResource(id = R.string.add_recipe_edit_recipe_title)
                        else
                            stringResource(id = R.string.add_recipe_add_new_recipe_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveRecipe() },
                expanded = isFabVisible,
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text(stringResource(id = R.string.add_recipe_save_recipe_button)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(Dimens.spacing_medium)
        ) {
            // Image Picker
            ImagePicker(
                selectedImageUri = uiState.selectedImageUri,
                existingImageUrl = uiState.existingImageUrl,
                onImageSelected = { viewModel.onCurrentImageUrlChange(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Basic Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(id = R.string.add_recipe_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_medium))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text(stringResource(id = R.string.add_recipe_description_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_medium))

                    OutlinedTextField(
                        value = uiState.servings,
                        onValueChange = viewModel::onServingsChange,
                        label = { Text("Servings") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Ingredients Section
            SectionHeader(
                title = stringResource(id = R.string.add_recipe_ingredients_title),
                icon = Icons.AutoMirrored.Filled.List
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            // Ingredient Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                    OutlinedTextField(
                        value = uiState.currentIngredientName,
                        onValueChange = viewModel::onCurrentIngredientNameChange,
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                    ) {
                        OutlinedTextField(
                            value = uiState.currentIngredientQuantity,
                            onValueChange = viewModel::onCurrentIngredientQuantityChange,
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = uiState.currentIngredientUnit,
                            onValueChange = viewModel::onCurrentIngredientUnitChange,
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.addIngredient()
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing_small))

                    Button(
                        onClick = {
                            viewModel.addIngredient()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(Dimens.spacing_small))
                        Text("Add Ingredient")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            // Ingredients List
            if (uiState.ingredients.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = Dimens.spacing_small)) {
                        uiState.ingredients.forEach { ingredient ->
                            IngredientListItem(
                                ingredient = ingredient,
                                onRemove = { viewModel.removeIngredient(ingredient) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_large))
//          ---- STEPS SECTION ---
            SectionHeader(
                title = "Steps",
                icon = Icons.Default.FormatListNumbered)

            Spacer(modifier = Modifier.height(8.dp))

            // Step Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                    OutlinedTextField(
                        value = uiState.currentStep,
                        onValueChange = viewModel::onCurrentStepChange,
                        label = { Text("Add a new step") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addStep()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_small))

                    Button(
                        onClick = {
                            viewModel.addStep()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(Dimens.spacing_small))
                        Text("Add Step")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            // Steps List
            if (uiState.steps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = Dimens.spacing_small)) {
                        uiState.steps.forEachIndexed { index, step ->
                            StepListItem(
                                stepNumber = index + 1,
                                step = step,
                                onRemove = { viewModel.removeStep(step) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Tags Section
            SectionHeader(
                title = "Tags",
                icon = Icons.Default.Tag
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            // Tag Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                    OutlinedTextField(
                        value = uiState.currentTag,
                        onValueChange = viewModel::onCurrentTagChange,
                        label = { Text("Tag (e.g., Dinner, Vegan, Quick)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addTag()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_small))

                    Button(
                        onClick = {
                            viewModel.addTag()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(Dimens.spacing_small))
                        Text("Add Tag")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            // Tags List
            if (uiState.tags.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    FlowRow(
                        modifier = Modifier.padding(Dimens.spacing_medium),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                    ) {
                        uiState.tags.forEach { tag ->
                            TagChip(
                                tag = tag,
                                onRemove = { viewModel.removeTag(tag) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_extra_large * 2))
        }
    }
}

@Composable
private fun IngredientListItem(
    ingredient: Ingredient,
    onRemove: () -> Unit
) {
    val formatter = DecimalFormat("0.##")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = Dimens.spacing_small)
            )
            Text(
                text = "${formatter.format(ingredient.quantity)} ${ingredient.unit} ${ingredient.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StepListItem(
    stepNumber: Int,
    step: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = step,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ImagePicker(
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    onImageSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onImageSelected(uri) }

    val imageToShow = selectedImageUri ?: existingImageUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .clickable {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageToShow != null) {
            AsyncImage(
                model = imageToShow,
                contentDescription = "Recipe image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
                Text(
                    "Tap to add image",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChip(
    tag: String,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = { },
        label = { Text(tag) },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove tag",
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = Dimens.spacing_small)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.spacing_small))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}