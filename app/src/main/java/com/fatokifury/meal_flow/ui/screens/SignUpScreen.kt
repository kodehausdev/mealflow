package com.fatokifury.meal_flow.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
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

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val isLoading: Boolean = false,
    // Hold-to-submit state
    val showHoldIndicator: Boolean = false,
    val holdProgress: Float = 0f,
    val isSubmitting: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.signUpUiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val emailInvalidError = stringResource(R.string.signup_invalid_email)
    val passwordLengthError = stringResource(R.string.signup_password_min_length)
    val confirmPasswordMismatchError = stringResource(R.string.signup_passwords_do_not_match)

//    val interactionSource = remember { MutableInteractionSource() }
//    val isPressed by interactionSource.collectIsPressedAsState()
//
//    // This should ideally be handled in the ViewModel, but for simplicity...
//    if (isPressed) {
//        authViewModel.onSignUpButtonPress()
//    } else {
//        authViewModel.onSignUpButtonRelease()
//    }

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
            text = stringResource(id = R.string.signup_create_account),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacing_extra_large))

        // Sign Up Card
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
                // Full Name Field
                Text(
                    text = stringResource(id = R.string.signup_full_name_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = { authViewModel.onSignUpFullNameChange(it) },
                    placeholder = { Text(stringResource(id = R.string.signup_full_name_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.spacing_large))

                // Email Field
                Text(
                    text = stringResource(id = R.string.login_email_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { authViewModel.onSignUpEmailChange(it) },
                    placeholder = { Text(stringResource(id = R.string.login_email_label)) },
                    isError = uiState.isEmailError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isEmailError) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_extra_small))
                    Text(
                        text = emailInvalidError,
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
                    value = uiState.password,
                    onValueChange = { authViewModel.onSignUpPasswordChange(it) },
                    placeholder = { Text(stringResource(id = R.string.login_password_label)) },
                    singleLine = true,
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        val image =
                            if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (uiState.isPasswordVisible)
                            stringResource(id = R.string.login_hide_password_description)
                        else
                            stringResource(id = R.string.login_show_password_description)

                        IconButton(onClick = { authViewModel.onSignUpTogglePasswordVisibility() }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    isError = uiState.isPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isPasswordError) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_extra_small))
                    Text(
                        text = passwordLengthError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_large))

                // Confirm Password Field
                Text(
                    text = stringResource(id = R.string.signup_confirm_password_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spacing_small))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { authViewModel.onSignUpConfirmPasswordChange(it) },
                    placeholder = { Text(stringResource(id = R.string.signup_confirm_password_label)) },
                    singleLine = true,
                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        val image =
                            if (uiState.isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (uiState.isConfirmPasswordVisible)
                            stringResource(id = R.string.login_hide_password_description)
                        else
                            stringResource(id = R.string.login_show_password_description)

                        IconButton(onClick = { authViewModel.onSignUpToggleConfirmPasswordVisibility() }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    isError = uiState.isConfirmPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isConfirmPasswordError) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_extra_small))
                    Text(
                        text = confirmPasswordMismatchError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacing_extra_large))

                // Submit Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {},
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth(),
//                        interactionSource = interactionSource
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.icon_size_small),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else if (uiState.showHoldIndicator) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(stringResource(id = R.string.signup_hold_to_submit))
                                CircularProgressIndicator(
                                    progress = { uiState.holdProgress },
                                    modifier = Modifier.size(Dimens.icon_size_medium),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = Dimens.spacing_extra_small
                                )
                            }
                        } else {
                            Text(stringResource(id = R.string.signup_button_text))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_large))

        // Login Prompt
        TextButton(
            onClick = { if (!uiState.isLoading) authViewModel.navigateToLogin() },
            enabled = !uiState.isLoading
        ) {
            Text(stringResource(id = R.string.signup_login_prompt))
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_large))
        // Terms and Policy
        Text(
            text = stringResource(id = R.string.signup_terms_and_policy),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = Dimens.spacing_medium)
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")
@Composable
fun SignUpScreenPreview() {
    MealFlowTheme {
        // SignUpScreen(authViewModel = hiltViewModel())
    }
}
