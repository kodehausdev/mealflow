package com.fatokifury.meal_flow.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.navigation.Screen
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.RecipeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    val recipe = uiState.recipe

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.recipe_detail_back_button))
                    }
                }
            )
        },
        floatingActionButton = {
            if (recipe != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Navigate with only the recipeId for a cleaner, more robust approach
                        viewModel.onEditRecipeClicked()
                        (Screen.AddRecipe.createRoute(recipe.id))
                    },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.add_recipe_edit_recipe_title)) },
                    text = { Text(stringResource(id = R.string.add_recipe_edit_recipe_title)) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (recipe != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.hero_image_height),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_launcher_background),
                            error = painterResource(id = R.drawable.ic_launcher_background)
                        )
                    }

                    item {
                        Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (recipe.description.isNotBlank()) {
                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.spacing_medium),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                                    SectionHeader(
                                        title = stringResource(id = R.string.recipe_detail_description_title),
                                        icon = Icons.Filled.Description
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spacing_small))
                                    Text(
                                        text = recipe.description,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    if (recipe.ingredients.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.spacing_medium))
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.spacing_medium),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                                    SectionHeader(
                                        title = stringResource(id = R.string.recipe_detail_ingredients_title),
                                        icon = Icons.AutoMirrored.Filled.List
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spacing_small))
                                    recipe.ingredients.forEach { ingredient ->
                                        Text(
                                            text = "• $ingredient",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(vertical = Dimens.spacing_extra_small)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (recipe.steps.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.spacing_medium))
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.spacing_medium),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
                                    SectionHeader(
                                        title = stringResource(id = R.string.recipe_detail_steps_title),
                                        icon = Icons.AutoMirrored.Filled.List
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spacing_small))
                                    recipe.steps.forEachIndexed { index, step ->
                                        Text(
                                            text = "${index + 1}. $step",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(vertical = Dimens.spacing_extra_small)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (recipe.tags.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.spacing_medium))
                            SectionHeader(
                                title = stringResource(id = R.string.recipe_detail_tags_title),
                                icon = Icons.Filled.Tag,
                                modifier = Modifier.padding(horizontal = Dimens.spacing_medium)
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing_small))
                            FlowRow(
                                modifier = Modifier.padding(horizontal = Dimens.spacing_medium),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                            ) {
                                recipe.tags.forEach { tag ->
                                    SuggestionChip(onClick = {}, label = { Text(tag) })
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(Dimens.floating_action_button_spacer))
                    }
                }
            } else {
                Text(
                    text = uiState.errorMessage ?: stringResource(id = R.string.recipe_detail_could_not_load),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
    }
}
@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    // The preview now works without a NavController
    RecipeDetailScreen()
}