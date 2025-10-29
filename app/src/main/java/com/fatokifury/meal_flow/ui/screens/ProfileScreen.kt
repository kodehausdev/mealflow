package com.fatokifury.meal_flow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onImportRecipeClick: () -> Unit, // Add a new lambda for navigation
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val appVersion = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName
    } catch (_: Exception) {
        "N/A"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Info Header
            Column(
                modifier = Modifier.padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = MaterialTheme.shapes.extraLarge, // Softer, more circular look
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                // Display Name (if available)
                Text(
                    text = currentUser?.displayName ?: "Valued Chef",
                    style = MaterialTheme.typography.headlineSmall
                )
                // Email
                Text(
                    text = currentUser?.email ?: "No email available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Divider for separation
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Action Items
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Import Recipe Item
                ListItem(
                    headlineContent = { Text("Import Recipe") },
                    supportingContent = { Text("Add a new recipe from a URL") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Import Recipe"
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { onImportRecipeClick() }
                )

                // Logout Item
                ListItem(
                    headlineContent = {
                        Text(
                            "Logout",
                            color = MaterialTheme.colorScheme.error // Use color to signify a destructive action
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable { onLogout() }
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            // App Version Info
            Text(
                text = "App Version: $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
