package com.fatokifury.meal_flow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fatokifury.meal_flow.ui.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {

    val user by viewModel.user.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (_: Exception) {
            "N/A"
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Profile Header
            ProfileHeader(
                displayName = currentUser?.displayName ?: "Valued Chef",
                email = currentUser?.email ?: "No email"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions Section
            SectionHeader(title = "Quick Actions")

            ProfileMenuItem(
                icon = Icons.Default.Link,
                title = "Import Recipe",
                subtitle = "Add a recipe from a URL",
                onClick = { viewModel.onImportRecipeClick() }
            )

            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences (Coming Soon)",
                enabled = false,
                onClick = { /* Will be implemented later */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // About Section
            SectionHeader(title = "About")

            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = appVersion,
                showArrow = false,
                onClick = {}
            )

            ProfileMenuItem(
                icon = Icons.Default.Star,
                title = "Rate App",
                subtitle = "Share your feedback on Play Store",
                onClick = { viewModel.rateApp(context) }
            )

            ProfileMenuItem(
                icon = Icons.Default.Share,
                title = "Share App",
                subtitle = "Tell your friends about MealFlow",
                onClick = { viewModel.shareApp(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Danger Zone
            SectionHeader(title = "Account", isError = true)

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Logout",
                subtitle = "Sign out of your account",
                isError = true,
                showArrow = false,
                onClick = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with gradient
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // User name
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Email chip
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Logout") },
        text = { Text("Are you sure you want to logout?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SectionHeader(
    title: String,
    isError: Boolean = false
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    isError: Boolean = false,
    showArrow: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (isError)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = if (enabled) 1f else 0.5f
                    ),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = if (enabled) 1f else 0.5f
                            ),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isError)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (enabled) 1f else 0.5f
                        )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (enabled) 1f else 0.5f
                        )
                    )
                }
            }

            // Arrow
            if (showArrow && enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}