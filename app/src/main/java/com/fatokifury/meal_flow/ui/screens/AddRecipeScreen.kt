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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.model.Ingredient
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.AddRecipeViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                icon = { Icon(Icons.Filled.Save, stringResource(id = R.string.add_recipe_save_recipe_button)) },
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

            ImagePicker(
                selectedImageUri = uiState.selectedImageUri,
                existingImageUrl = uiState.existingImageUrl,
                onImageSelected = { viewModel.onCurrentImageUrlChange(it) }
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

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

                    Spacer(modifier = Modifier.height(Dimens.spacing_medium))

                    OutlinedTextField(
                        value = uiState.servings,
                        onValueChange = viewModel::onServingsChange,
                        label = { Text("Servings") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

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
                        uiState.ingredients.forEach { ingredient ->
                            IngredientListItem(
                                ingredient = ingredient,
                                onRemove = { viewModel.removeIngredient(ingredient) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacing_small))
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                AddIngredientInput(
                    name = uiState.currentIngredientName,
                    quantity = uiState.currentIngredientQuantity,
                    unit = uiState.currentIngredientUnit,
                    onNameChange = viewModel::onCurrentIngredientNameChange,
                    onQuantityChange = viewModel::onCurrentIngredientQuantityChange,
                    onUnitChange = viewModel::onCurrentIngredientUnitChange,
                    onAdd = {
                        viewModel.addIngredient()
                        focusManager.clearFocus()
                    },
                    focusManager = focusManager
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            // The rest of the screen remains the same
        }
    }
}

@Composable
private fun AddIngredientInput(
    name: String,
    quantity: String,
    unit: String,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAdd: () -> Unit,
    focusManager: FocusManager
) {
    Row(
        modifier = Modifier.padding(Dimens.spacing_medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Ingredient") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Qty") },
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
        )
        OutlinedTextField(
            value = unit,
            onValueChange = onUnitChange,
            label = { Text("Unit") },
            modifier = Modifier.width(90.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAdd()
            })
        )
        IconButton(onClick = onAdd) {
            Icon(Icons.Default.AddCircle, contentDescription = "Add Ingredient")
        }
    }
}

@Composable
private fun IngredientListItem(
    ingredient: Ingredient,
    onRemove: () -> Unit
) {
    val quantityFormatter = DecimalFormat("0.##")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "• ${quantityFormatter.format(ingredient.quantity)} ${ingredient.unit} ${ingredient.name}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(Dimens.icon_button_size_small)) {
            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove Ingredient")
        }
    }
}

@Composable
private fun ImagePicker(
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    val imageToShow = selectedImageUri ?: existingImageUrl

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
        if (imageToShow != null) {
            AsyncImage(
                model = imageToShow,
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
            style = MaterialTheme.typography.titleMedium
        )
        if (optional) {
            Spacer(modifier = Modifier.width(Dimens.spacing_small))
            Text(
                text = stringResource(id = R.string.optional_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
