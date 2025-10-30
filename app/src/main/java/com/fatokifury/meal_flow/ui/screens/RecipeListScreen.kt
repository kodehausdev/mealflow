package com.fatokifury.meal_flow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.RecipeListUiState
import com.fatokifury.meal_flow.ui.viewmodels.RecipeListViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    onlogout: () -> Unit // Keep for now but not used (moved to Profile)
) {
    val lazyGridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    // Dialog state from ViewModel
    val showImportDialog by viewModel.showImportDialog
    val importUrl by viewModel.importUrl

    if (showImportDialog) {
        ImportUrlDialog(
            urlInput = importUrl,
            onUrlChange = viewModel::onImportUrlChange,
            onDismiss = viewModel::onImportDialogDismiss,
            onImport = { viewModel.onImportFromUrl(importUrl.text) }
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = uiState.recipes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    // Listen for user messages
    val undoActionLabel = stringResource(R.string.undo)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { userMessage ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = userMessage.message,
                    actionLabel = if (userMessage.recipe != null) undoActionLabel else null,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    userMessage.recipe?.let { viewModel.undoDelete(it) }
                }
            }
            viewModel.userMessageShown()
        }
    }

    // FAB visibility based on scroll position
    LaunchedEffect(lazyListState, lazyGridState, uiState.isGridView) {
        snapshotFlow {
            if (uiState.isGridView) {
                lazyGridState.firstVisibleItemIndex
            } else {
                lazyListState.firstVisibleItemIndex
            }
        }.collect { firstVisibleItem ->
            isFabVisible = firstVisibleItem == 0
        }
    }

    // FAB animation
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.9f else 1f,
        label = "fab_scale"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(stringResource(id = R.string.recipe_list_my_recipes_title))
                        Text(
                            text = context.resources.getQuantityString(
                                R.plurals.recipe_count_subtitle,
                                uiState.recipes.size,
                                uiState.recipes.size
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Card-style Import button
                    Surface(
                        onClick = viewModel::onImportRecipeClicked,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Import",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState.recipes.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::onAddRecipeClicked,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary, // switched from primaryContainer
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    expanded = isFabVisible,
                    icon = {
                        Icon(Icons.Filled.Add, stringResource(R.string.add_recipe_fab_text))
                    },
                    text = {
                        Text(
                            stringResource(R.string.add_recipe_fab_text),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    modifier = Modifier.scale(fabScale),
                    interactionSource = fabInteractionSource
                )
            }
        }
    ) { paddingValues ->
        RecipeListContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            viewModel = viewModel,
            filteredRecipes = filteredRecipes,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            listState = lazyListState,
            gridState = lazyGridState
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    modifier: Modifier = Modifier,
    uiState: RecipeListUiState,
    viewModel: RecipeListViewModel,
    filteredRecipes: List<Recipe>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search and View Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
        ) {
            var active by remember { mutableStateOf(false) }

            DockedSearchBar(
                modifier = Modifier.weight(1f),
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                }
            ) {
                // Search history/suggestions can go here
            }

            IconToggleButton(
                checked = uiState.isGridView,
                onCheckedChange = { viewModel.onToggleView() }
            ) {
                Icon(
                    if (uiState.isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle view"
                )
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.recipes.isEmpty() -> {
                EmptyState(
                    onAddRecipe = viewModel::onAddRecipeClicked,
                    onImportRecipe = viewModel::onImportRecipeClicked
                )
            }
            filteredRecipes.isEmpty() -> {
                EmptySearchState(searchQuery = searchQuery)
            }
            else -> {
                AnimatedContent(
                    targetState = uiState.isGridView,
                    label = "view_type_animation",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }
                ) { isGrid ->
                    if (isGrid) {
                        RecipeGridView(
                            state = gridState,
                            recipes = filteredRecipes,
                            onRecipeClick = { viewModel.onRecipeSelected(it.id) },
                            onRecipeDelete = { viewModel.deleteRecipe(it) }
                        )
                    } else {
                        RecipeListView(
                            state = listState,
                            recipes = filteredRecipes,
                            onRecipeClick = { viewModel.onRecipeSelected(it.id) },
                            onRecipeDelete = { viewModel.deleteRecipe(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddRecipe: () -> Unit,
    onImportRecipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacing_extra_large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with gradient background
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.empty_state_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.empty_state_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Single primary action button
        Button(
            onClick = onAddRecipe,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.add_recipe),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary action
        TextButton (
            onClick = onImportRecipe,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.empty_state_import_recipe),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EmptySearchState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacing_extra_large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_medium))

        Text(
            text = "No recipes found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_small))

        Text(
            text = "No results for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RecipeListView(
    state: LazyListState,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDelete: (Recipe) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimens.spacing_medium,
            vertical = Dimens.spacing_small
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
        items(
            items = recipes,
            key = { it.id }
        ) { recipe ->
            val dismissBoxState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    when (dismissValue) {
                        SwipeToDismissBoxValue.StartToEnd,
                        SwipeToDismissBoxValue.EndToStart -> {
                            onRecipeDelete(recipe)
                            true
                        }
                        else -> false
                    }
                },
                positionalThreshold = { distance -> distance * 0.25f }
            )

            SwipeToDismissBox(
                modifier = Modifier
                    .animateItemPlacement()
                    .clip(RoundedCornerShape(Dimens.spacing_small)), // Clip to prevent overflow
                state = dismissBoxState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    val color by animateColorAsState(
                        targetValue = when (dismissBoxState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd,
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "dismiss_bg_color"
                    )

                    val alignment = when (dismissBoxState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }

                    val iconScale by animateFloatAsState(
                        targetValue = if (dismissBoxState.targetValue != SwipeToDismissBoxValue.Settled) 1f else 0.75f,
                        label = "dismiss_icon_scale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, RoundedCornerShape(Dimens.spacing_small))
                            .padding(horizontal = Dimens.spacing_large),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.recipe_list_delete_icon),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.scale(iconScale)
                        )
                    }
                }
            ) {
                // Wrap the card in a Box to prevent z-index issues
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EnhancedRecipeListItem(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeTagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.spacing_small),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Dimens.spacing_small, vertical = Dimens.spacing_extra_small)
        )
    }
}

@Composable
private fun RecipeGridView(
    recipes: List<Recipe>,
    state: LazyGridState,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDelete: (Recipe) -> Unit
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.spacing_medium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeGridItem(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}

// Also update EnhancedRecipeListItem to ensure proper elevation
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedRecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp, // Increased from Dimens.elevation_small
            pressedElevation = 4.dp,
            focusedElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Dimens.spacing_small) // Ensure consistent shape
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.spacing_small,
                vertical = Dimens.spacing_medium
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
        ) {
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(Dimens.spacing_medium)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_restaurant),
                    error = painterResource(id = R.drawable.ic_restaurant)
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = Dimens.spacing_extra_small)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
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

                if (recipe.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_small))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
                    ) {
                        recipe.tags.take(4).forEach { tag ->
                            RecipeTagChip(tag = tag)
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecipeGridItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = Dimens.elevation_small
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_restaurant),
                    error = painterResource(id = R.drawable.ic_restaurant)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.padding(Dimens.spacing_medium)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
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

@Composable
private fun ImportUrlDialog(
    urlInput: TextFieldValue,
    onUrlChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small)
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(id = R.string.recipe_list_import_dialog_title))
            }
        },
        text = {
            Column {
                Text(
                    text = "Paste a recipe URL to import",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_medium))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    label = { Text(stringResource(id = R.string.recipe_list_enter_url_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onImport(urlInput.text) }) {
                Text(stringResource(id = R.string.recipe_list_import_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.recipe_list_cancel_button))
            }
        }
    )
}