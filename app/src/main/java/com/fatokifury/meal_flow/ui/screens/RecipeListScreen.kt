package com.fatokifury.meal_flow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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

/** Extra space at the bottom of both lists so the last card can scroll clear of the FAB. */
private val FabClearance = 88.dp

/** How far (as a fraction of the card width) a swipe must travel to ask for deletion. */
private const val SwipeDeleteThreshold = 0.6f

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    onlogout: () -> Unit // Unused: logout lives in Profile. Kept so existing call sites compile.
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Import dialog state lives in the ViewModel
    val showImportDialog by viewModel.showImportDialog
    val importUrl by viewModel.importUrl

    var searchQuery by remember { mutableStateOf("") }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    val filteredRecipes = remember(uiState.recipes, searchQuery) {
        uiState.recipes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showImportDialog) {
        ImportUrlDialog(
            urlInput = importUrl,
            onUrlChange = viewModel::onImportUrlChange,
            onDismiss = viewModel::onImportDialogDismiss,
            onImport = { viewModel.onImportFromUrl(importUrl.text) }
        )
    }

    recipeToDelete?.let { recipe ->
        DeleteRecipeDialog(
            recipe = recipe,
            onConfirm = {
                viewModel.deleteRecipe(recipe)
                recipeToDelete = null
            },
            onDismiss = { recipeToDelete = null }
        )
    }

    // Snackbar messages from the ViewModel (with Undo after a delete)
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RecipeListTopBar(
                recipeCount = uiState.recipes.size,
                onImportClick = viewModel::onImportRecipeClicked,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState.recipes.isNotEmpty()) {
                AddRecipeFab(onClick = viewModel::onAddRecipeClicked)
            }
        }
    ) { paddingValues ->
        RecipeListContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            filteredRecipes = filteredRecipes,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onToggleView = viewModel::onToggleView,
            onRecipeClick = { viewModel.onRecipeSelected(it.id) },
            onRecipeDeleteRequest = { recipeToDelete = it },
            onAddRecipe = viewModel::onAddRecipeClicked,
            onImportRecipe = viewModel::onImportRecipeClicked
        )
    }
}

// ---------------------------------------------------------------------------
// Top bar, FAB
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListTopBar(
    recipeCount: Int,
    onImportClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val context = LocalContext.current

    LargeTopAppBar(
        title = {
            Column {
                Text(stringResource(id = R.string.recipe_list_my_recipes_title))
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.recipe_count_subtitle,
                        recipeCount,
                        recipeCount
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = { ImportButton(onClick = onImportClick) },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
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
}

/** Compact, icon-only button (WhatsApp style). Never grows, so it can't cover card text. */
@Composable
private fun AddRecipeFab(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_recipe_fab_text)
        )
    }
}

// ---------------------------------------------------------------------------
// Content (search row, empty states, list / grid)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    modifier: Modifier = Modifier,
    uiState: RecipeListUiState,
    filteredRecipes: List<Recipe>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleView: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDeleteRequest: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onImportRecipe: () -> Unit
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchAndToggleRow(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            isGridView = uiState.isGridView,
            onToggleView = onToggleView
        )

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.recipes.isEmpty() -> {
                EmptyState(onAddRecipe = onAddRecipe, onImportRecipe = onImportRecipe)
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
                            onRecipeClick = onRecipeClick
                        )
                    } else {
                        RecipeListView(
                            state = listState,
                            recipes = filteredRecipes,
                            onRecipeClick = onRecipeClick,
                            onRecipeDeleteRequest = onRecipeDeleteRequest
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndToggleRow(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isGridView: Boolean,
    onToggleView: () -> Unit
) {
    var active by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_medium, vertical = Dimens.spacing_small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
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
            checked = isGridView,
            onCheckedChange = { onToggleView() }
        ) {
            Icon(
                if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                contentDescription = "Toggle view"
            )
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

        TextButton(
            onClick = onImportRecipe,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
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

// ---------------------------------------------------------------------------
// List view + swipe-to-delete
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeListView(
    state: LazyListState,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDeleteRequest: (Recipe) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.spacing_medium,
            end = Dimens.spacing_medium,
            top = Dimens.spacing_small,
            bottom = FabClearance
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
        items(
            items = recipes,
            key = { it.id }
        ) { recipe ->
            SwipeToDeleteRecipeItem(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) },
                onDeleteRequest = { onRecipeDeleteRequest(recipe) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

/**
 * Swipe right-to-left to ask for deletion.
 *
 * The swipe never dismisses the row by itself: `confirmValueChange` always returns false, so the
 * card springs back and the confirmation dialog decides. This matters because SwipeToDismissBox
 * also accepts a fast fling regardless of how far the card travelled, which made quick vertical
 * scrolls with a little sideways drift delete recipes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDeleteRequest()
            false
        },
        positionalThreshold = { distance -> distance * SwipeDeleteThreshold }
    )

    SwipeToDismissBox(
        modifier = modifier.clip(RoundedCornerShape(Dimens.spacing_small)),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { DeleteSwipeBackground(dismissState) }
    ) {
        // Wrapped in a Box to avoid z-index issues while the card is moving
        Box(modifier = Modifier.fillMaxWidth()) {
            EnhancedRecipeListItem(recipe = recipe, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteSwipeBackground(state: SwipeToDismissBoxState) {
    val armed = state.targetValue == SwipeToDismissBoxValue.EndToStart

    val color by animateColorAsState(
        targetValue = if (armed) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "dismiss_bg_color"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (armed) 1f else 0.75f,
        label = "dismiss_icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(Dimens.spacing_small))
            .padding(horizontal = Dimens.spacing_large),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(id = R.string.recipe_list_delete_icon),
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.scale(iconScale)
        )
    }
}

@Composable
private fun DeleteRecipeDialog(
    recipe: Recipe,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete recipe?") },
        text = { Text("\"${recipe.title}\" will be removed from your recipes.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---------------------------------------------------------------------------
// Grid view
// ---------------------------------------------------------------------------

@Composable
private fun RecipeGridView(
    recipes: List<Recipe>,
    state: LazyGridState,
    onRecipeClick: (Recipe) -> Unit
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.spacing_medium,
            end = Dimens.spacing_medium,
            top = Dimens.spacing_medium,
            bottom = FabClearance
        ),
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

// ---------------------------------------------------------------------------
// Cards
// ---------------------------------------------------------------------------

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
            modifier = Modifier.padding(
                horizontal = Dimens.spacing_small,
                vertical = Dimens.spacing_extra_small
            )
        )
    }
}

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
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            focusedElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Dimens.spacing_small)
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

            Column(modifier = Modifier.padding(Dimens.spacing_medium)) {
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

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

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