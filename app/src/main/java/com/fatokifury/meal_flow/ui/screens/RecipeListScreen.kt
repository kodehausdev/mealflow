package com.fatokifury.meal_flow.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.RecipeListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // <-- GET THE CONTEXT HERE, in the Composable scope

    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.recipe_list_my_recipes_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = stringResource(id = R.string.recipe_list_import_from_url)
                        )
                    }
                    IconButton(onClick = { viewModel.onMealCalendarClicked() }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = stringResource(id = R.string.recipe_list_open_meal_calendar)
                        )
                    }
                    IconButton(onClick = { viewModel.onLogoutClicked() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddRecipeClicked() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.recipe_list_add_new_recipe)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.recipes.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.recipe_list_no_recipes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Dimens.spacing_medium)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
                    ) {                        items(uiState.recipes, key = { recipe -> recipe.id }) { recipe ->
                            val dismissBoxState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                        viewModel.deleteRecipe(recipe)
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissBoxState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = when (dismissBoxState.targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        },
                                        label = "dismiss_bg_color"
                                    )

                                    val alignment = when (dismissBoxState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }

                                    val scale by animateFloatAsState(
                                        targetValue = if (dismissBoxState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                                        label = "dismiss_icon_scale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = Dimens.spacing_large),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.recipe_list_delete_icon),
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                }
                            ) {
                                RecipeListItem(
                                    recipe = recipe,
                                    onClick = { viewModel.onRecipeSelected(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text(stringResource(id = R.string.recipe_list_import_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(stringResource(id = R.string.recipe_list_enter_url_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val url = urlInput.text.trim()
                        if (url.isNotBlank()) {
                            viewModel.onImportRecipeClicked(url)
                            showUrlDialog = false
                            urlInput = TextFieldValue("")
                        } else {
                            Toast.makeText(context, R.string.recipe_list_empty_url, Toast.LENGTH_SHORT).show()                        }
                    }
                ) {
                    Text(stringResource(id = R.string.recipe_list_import_button))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showUrlDialog = false
                    urlInput = TextFieldValue("")
                }) {
                    Text(stringResource(id = R.string.recipe_list_cancel_button))
                }
            }
        )
    }
}

@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spacing_medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(Dimens.list_item_image_size)
                    .clip(RoundedCornerShape(Dimens.spacing_small)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Dimens.spacing_extra_small)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Recipe List Screen")
@Composable
fun RecipeListScreenPreview() {
    // Preview will not have working navigation
    // RecipeListScreen()
}
