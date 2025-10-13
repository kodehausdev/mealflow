package com.fatokifury.meal_flow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.fatokifury.meal_flow.R
import com.fatokifury.meal_flow.ui.theme.Dimens
import com.fatokifury.meal_flow.ui.theme.MealFlowTheme
import com.fatokifury.meal_flow.ui.viewmodels.AuthViewModel

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val isPasswordVisible: Boolean = false,
    val isEmailError: Boolean = false,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.loginUiState.collectAsState()
    val emailErrorMessage = stringResource(id = R.string.login_invalid_email_format)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.spacing_large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title Section
        Text(
            text = stringResource(id = R.string.login_app_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_small))
        Text(
            text = stringResource(id = R.string.login_welcome_back),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_extra_large))

        // Login Card
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
                    .padding(Dimens.spacing_large)
            ) {
                // Email Field
                Text(
                    text = stringResource(id = R.string.login_email_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { authViewModel.onLoginEmailChange(it) },
                    placeholder = { Text(stringResource(id = R.string.login_email_label)) },
                    singleLine = true,
                    isError = uiState.isEmailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isEmailError && uiState.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_extra_small))
                    Text(
                        text = emailErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_large))

                // Password Field
                Text(
                    text = stringResource(id = R.string.login_password_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))

                OutlinedTextField(
                    value = uiState.pass,
                    onValueChange = { authViewModel.onLoginPasswordChange(it) },
                    placeholder = { Text(stringResource(id = R.string.login_password_label)) },
                    singleLine = true,
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        val image = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (uiState.isPasswordVisible)
                            stringResource(id = R.string.login_hide_password_description)
                        else
                            stringResource(id = R.string.login_show_password_description)

                        IconButton(onClick = { authViewModel.onLoginTogglePasswordVisibility() }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_extra_large))

                // Submit Button
                Button(
                    onClick = { authViewModel.loginUser() },
                    enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.pass.isNotBlank() && !uiState.isEmailError,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.icon_size_small),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(id = R.string.login_button_text))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_large))

        // Sign Up Prompt
        TextButton(onClick = { if (!uiState.isLoading) authViewModel.navigateToSignUp() }, enabled = !uiState.isLoading) {
            Text(stringResource(id = R.string.login_signup_prompt))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MealFlowTheme {
        // This preview will not have a working ViewModel, so it is commented out.
        // To see a preview, you would need to create a mock AuthViewModel.
        // LoginScreen(authViewModel = hiltViewModel())
    }
}
