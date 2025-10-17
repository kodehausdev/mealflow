package com.fatokifury.meal_flow.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.AddRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ADD THIS NEW BLOCK
    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collect { message ->Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.recipeId != null) stringResource(id = R.string.add_recipe_edit_recipe_title) else stringResource(id = R.string.add_recipe_add_new_recipe_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveRecipe() },
                icon = { Icon(Icons.Filled.Add, stringResource(id = R.string.add_recipe_save_recipe_button)) },
                text = { Text(stringResource(id = R.string.add_recipe_save_recipe_button)) },
                expanded = true,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimens.spacing_medium)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            // Image Picker
            ImagePicker(
                selectedImageUri = uiState.selectedImageUri,
                onImageSelected = { viewModel.onCurrentImageUrlChange(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Title Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
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
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacing_medium))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text(stringResource(id = R.string.add_recipe_description_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Dimens.text_field_height),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        maxLines = 5,
                        shape = MaterialTheme.shapes.medium
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

            if (uiState.ingredients.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = Dimens.card_padding_vertical)) {
                        uiState.ingredients.forEachIndexed { index, ingredient ->
                            EnhancedListItem(
                                text = ingredient,
                                onRemove = { viewModel.removeIngredient(ingredient) },
                                isLastItem = index == uiState.ingredients.lastIndex
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
            }

            AddItemRow(
                value = uiState.currentIngredient,
                onValueChange = viewModel::onCurrentIngredientChange,
                label = stringResource(id = R.string.add_recipe_add_ingredient_label),
                onAdd = {
                    viewModel.addIngredient()
                    focusManager.clearFocus()
                },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Steps Section
            SectionHeader(
                title = stringResource(id = R.string.add_recipe_steps_title),
                icon = Icons.AutoMirrored.Filled.List
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            if (uiState.steps.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = Dimens.card_padding_vertical)) {
                        uiState.steps.forEachIndexed { index, step ->
                            EnhancedListItem(
                                text = "${index + 1}. $step",
                                onRemove = { viewModel.removeStep(step) },
                                isLastItem = index == uiState.steps.lastIndex
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
            }

            AddItemRow(
                value = uiState.currentStep,
                onValueChange = viewModel::onCurrentStepChange,
                label = stringResource(id = R.string.add_recipe_add_step_label),
                onAdd = {
                    viewModel.addStep()
                    focusManager.clearFocus()
                },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // Tags Section
            SectionHeader(
                title = stringResource(id = R.string.add_recipe_tags_title),
                icon = Icons.Default.Tag,
                optional = true
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_small))

            if (uiState.tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                ) {
                    uiState.tags.forEach { tag ->
                        TagChip(
                            text = tag,
                            onRemove = { viewModel.removeTag(tag) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
            }

            AddItemRow(
                value = uiState.currentTag,
                onValueChange = viewModel::onCurrentTagChange,
                label = stringResource(id = R.string.add_recipe_add_tag_label),
                onAdd = {
                    viewModel.addTag()
                    focusManager.clearFocus()
                },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(Dimens.floating_action_button_spacer))
        }
    }
}

@Composable
private fun ImagePicker(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.image_picker_height)
            .clip(MaterialTheme.shapes.large)
            .border(
                width = Dimens.spacing_extra_small / 2,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .clickable {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected recipe image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "Add Image",
                    modifier = Modifier.size(Dimens.icon_size_large),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
                Text("Tap to add an image", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    optional: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = Dimens.card_padding_vertical)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.section_header_icon_size)
        )
        Spacer(modifier = Modifier.width(Dimens.spacing_small))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (optional) {
            Spacer(modifier = Modifier.width(Dimens.spacing_small))
            Text(
                text = "(Optional)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedListItem(
    text: String,
    onRemove: () -> Unit,
    isLastItem: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Remove item")
        }
    }
    if (!isLastItem) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.spacing_medium))
    }
}

@Composable
private fun AddItemRow(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onAdd: () -> Unit,
    focusManager: FocusManager
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAdd()
                focusManager.clearFocus()
            })
        )
        Spacer(modifier = Modifier.width(Dimens.spacing_small))
        IconButton(onClick = onAdd, enabled = value.isNotBlank()) {
            Icon(Icons.Filled.Add, contentDescription = "Add item")
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = { /* Nothing to do on click */ },
        label = { Text(text) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                modifier = Modifier
                    .size(Dimens.icon_size_small)
                    .clickable { onRemove() }
            )
        }
    )
}
