package com.fatokifury.meal_flow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.navigation.Screen
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.viewmodels.ImportRecipeViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRecipeScreen(
    viewModel: ImportRecipeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // This LaunchedEffect will now only run once to trigger the import.
    // The ViewModel is responsible for getting the URL from SavedStateHandle.
    LaunchedEffect(Unit) {
        viewModel.startImport()
    }

    // This LaunchedEffect correctly handles showing error messages.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    // This LaunchedEffect handles the navigation *after* a successful import.
    LaunchedEffect(uiState.newRecipeId) {
        val recipeId = uiState.newRecipeId
        if (recipeId != null) {
            Toast.makeText(context, "Recipe Imported Successfully!", Toast.LENGTH_SHORT).show()
            // The ViewModel now handles the navigation logic, including popping the back stack.
            viewModel.navigationHandled() // Reset the state to prevent re-navigation
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isLoading)
                            stringResource(id = R.string.import_recipe_importing_title)
                        else
                            stringResource(id = R.string.import_recipe_import_title)
                    )
                },
                // FIX: Add the back button to the TopAppBar
                navigationIcon = {
                    IconButton(onClick = { viewModel.onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(Dimens.spacing_large),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    LoadingCard(url = uiState.importUrl)
                }
                uiState.errorMessage != null -> {
                    // FIX: Pass the ViewModel's navigation function to the ErrorCard
                    ErrorCard(
                        errorMessage = uiState.errorMessage,
                        onGoBack = { viewModel.onNavigateBack() }
                    )
                }
                uiState.newRecipeId != null -> {
                    SuccessCard()
                }
                // The ViewModel now determines if the URL is invalid.
                // We can simplify the UI logic.
                else -> {
                    // We can show a generic "getting ready" card or the loading card again.
                    // Assuming if not loading and no error, it's getting ready.
                    ReadyToImportCard(url = uiState.importUrl)
                }
            }
        }
    }
}


@Composable
private fun LoadingCard(url: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_extra_large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.icon_size_large),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(id = R.string.import_recipe_processing_url),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            Text(
                text = url ?: stringResource(id = R.string.import_recipe_no_url),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(errorMessage: String?, onGoBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_extra_large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(Dimens.icon_size_large),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(id = R.string.import_recipe_import_failed),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            Text(
                text = errorMessage ?: stringResource(id = R.string.import_recipe_unknown_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Button(
                onClick = onGoBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.import_recipe_go_back_button))
            }
        }
    }
}

@Composable
private fun SuccessCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_extra_large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(Dimens.icon_size_large),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(id = R.string.import_recipe_preparing_recipe),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InvalidUrlCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_extra_large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(Dimens.icon_size_large),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(id = R.string.import_recipe_invalid_url),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReadyToImportCard(url: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_extra_large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.icon_size_large),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_large))

            Text(
                text = stringResource(id = R.string.import_recipe_ready_to_import),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_medium))

            Text(
                text = url ?: stringResource(id = R.string.import_recipe_no_url_provided),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}