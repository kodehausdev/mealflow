package com.fatokifury.meal_flow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.RecipeListViewModel
import kotlinx.coroutines.launch

private enum class ViewType {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    onlogout: () -> Unit
) {
//    // --- START OF TEMPORARY CODE ---
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("Recipe List Screen reached successfully!")
//    }
//    // --
//}
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current

    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf(TextFieldValue("")) }
    val undoActionLabel = stringResource(R.string.undo)

    var searchQuery by remember { mutableStateOf("") }
    var viewType by remember { mutableStateOf(ViewType.LIST) }

    val filteredRecipes = uiState.recipes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    // Listen for user messages from the ViewModel
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

    // FAB animation state
    val expandedFab by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by  animateFloatAsState(targetValue = if (isFabPressed) 0.9f else 1f, label = "fab_scale")

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
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_small)) {
                        IconButton(onClick = { showUrlDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = stringResource(id = R.string.recipe_list_import_from_url)
                            )
                        }
                        IconButton(onClick = onlogout) {                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = stringResource(id = R.string.recipe_list_open_meal_calendar)
                            )
                        }
                        IconButton(onClick = { viewModel.onLogoutClicked() }) {
                            Icon(
                                imageVector = Icons.Rounded.Logout,
                                contentDescription = stringResource(R.string.logout)
                            )
                        }

                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAddRecipeClicked() },
                shape = RoundedCornerShape(Dimens.fab_corner_radius),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                expanded = expandedFab,
                icon = { Icon(Icons.Filled.Add, stringResource(id = R.string.add_recipe_fab_text)) },
                text = { Text(stringResource(id = R.string.add_recipe_fab_text)) },
                modifier = Modifier.scale(fabScale),
                interactionSource = fabInteractionSource
            )
        }
    ) { paddingValues ->
        RecipeListContent(
            modifier = Modifier.padding(paddingValues),
            isLoading = uiState.isLoading,
            recipes = uiState.recipes,
            filteredRecipes = filteredRecipes,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            viewType = viewType,
            onViewTypeChange = { viewType = it },
            onRecipeClick = { viewModel.onRecipeSelected(it.id) },
            onRecipeDelete = { viewModel.deleteRecipe(it) },
            onAddRecipe = { viewModel.onAddRecipeClicked() },
            onImportRecipe = { showUrlDialog = true }
        )
    }

    if (showUrlDialog) {
        ImportUrlDialog(
            urlInput = urlInput,
            onUrlChange = { urlInput = it },
            onDismiss = { showUrlDialog = false },
            onImport = {
                val url = urlInput.text.trim()
                if (url.isNotBlank()) {
                    viewModel.onImportRecipeClicked(url)
                    showUrlDialog = false
                    urlInput = TextFieldValue("")
                } else {
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.recipe_list_empty_url)) }
                }
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    recipes: List<Recipe>,
    filteredRecipes: List<Recipe>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewType: ViewType,
    onViewTypeChange: (ViewType) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDelete: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onImportRecipe: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search and View Options
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
                    if (active) {
                        IconButton(onClick = { if (searchQuery.isNotEmpty()) onSearchQueryChange("") else active = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear or close search")
                        }
                    }
                }
            ) {
                filteredRecipes.take(5).forEach { recipe ->
                    ListItem(
                        headlineContent = { Text(recipe.title) },
                        modifier = Modifier
                            .clickable {
                                onSearchQueryChange(recipe.title)
                                active = false
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            IconToggleButton(
                checked = viewType == ViewType.GRID,
                onCheckedChange = { onViewTypeChange(if (it) ViewType.GRID else ViewType.LIST) }) {
                Icon(
                    if (viewType == ViewType.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle view"
                )
            }
        }

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty()) {
            EmptyState(onAddRecipe = onAddRecipe, onImportRecipe = onImportRecipe)
        } else if (filteredRecipes.isEmpty()) {
            EmptySearchState(searchQuery = searchQuery)
        } else {
            AnimatedContent(
                targetState = viewType,
                label = "view_type_animation",
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                }
            ) { targetViewType ->
                when (targetViewType) {
                    ViewType.LIST -> RecipeListView(
                        recipes = filteredRecipes,
                        onRecipeClick = onRecipeClick,
                        onRecipeDelete = onRecipeDelete
                    )

                    ViewType.GRID -> RecipeGridView(
                        recipes = filteredRecipes,
                        onRecipeClick = onRecipeClick,
                        onRecipeDelete = onRecipeDelete
                    )
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
        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_medium))

        Text(
            text = stringResource(id = R.string.empty_state_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_small))

        Text(
            text = stringResource(id = R.string.empty_state_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_extra_large))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
        ) {
            Button(
                onClick = onAddRecipe,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.spacing_small))
                Text("Create Recipe")
            }

            OutlinedButton(
                onClick = onImportRecipe,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Link, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.spacing_small))
                Text("Import")
            }
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
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDelete: (Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimens.spacing_medium,
            vertical = Dimens.spacing_small
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing_medium)
    ) {
        items(recipes, key = { it.id }) { recipe ->
            val dismissBoxState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                        onRecipeDelete(recipe)
                        true
                    } else false
                }
            )


            SwipeToDismissBox(
                modifier = Modifier.animateItemPlacement(),
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
                            .background(color, RoundedCornerShape(Dimens.spacing_small))
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
                EnhancedRecipeListItem(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) }
                )
            }
        }
    }
}

@Composable
private fun RecipeGridView(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRecipeDelete: (Recipe) -> Unit
) {
    LazyVerticalGrid(
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
            defaultElevation = Dimens.elevation_small
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing_small, vertical = Dimens.spacing_medium),
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

            Column(Modifier.weight(1f)) {
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
                    placeholder = painterResource(id = R.drawable.placeholder_food),
                    error = painterResource(id = R.drawable.placeholder_food)
                )

                // Gradient overlay for better text readability
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
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.recipe_list_cancel_button))
            }
        }
    )
}

// ADD THIS NEW PREVIEW
@Preview(name = "Recipe List Content", showBackground = true)
@Composable
private fun RecipeListContentPreview() {
    // Create some fake recipe data for the preview
    val sampleRecipes = listOf(
        Recipe(id = "1", title = "Classic Pancakes", description = "Fluffy and delicious pancakes for breakfast.", imageUrl = ""),
        Recipe(id = "2", title = "Spaghetti Carbonara", description = "A creamy and classic Italian pasta dish.", imageUrl = "")
    )

    // Use your app's theme
    MaterialTheme {
        RecipeListContent(
            isLoading = false,
            recipes = sampleRecipes,
            filteredRecipes = sampleRecipes,
            searchQuery = "",
            viewType = ViewType.LIST,
            onSearchQueryChange = {},
            onViewTypeChange = {},
            onRecipeClick = {},
            onRecipeDelete = {},
            onAddRecipe = {},
            onImportRecipe = {}
        )
    }
}
