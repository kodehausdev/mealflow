
package com.fatokifury.meal_flow.ui.screens

import android.util.Patterns // For email validation
import androidx.compose.foundation.ExperimentalFoundationApi // Required for combinedClickable
import androidx.compose.foundation.combinedClickable // Required for combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // For potential progress overlay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fatokifury.meal_flow.ui.theme.MealFlowTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (fullName: String, email: String, pass: String, confirmPass: String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isEmailError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }

    var isPasswordError by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf("") }

    var isConfirmPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordErrorMessage by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // For hold-to-submit
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showHoldIndicator by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    val holdDurationMillis = 1500L // 1.5 seconds to hold


    // Validation logic (can be extracted to a helper function or ViewModel)
    fun validateFields(): Boolean {
        var canProceed = true
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailError = true
            emailErrorMessage = if (email.isEmpty()) "Email cannot be empty." else "Please enter a valid email."
            canProceed = false
        } else {
            isEmailError = false
        }

        if (password.isEmpty() || password.length < 6) {
            isPasswordError = true
            passwordErrorMessage = if (password.isEmpty()) "Password cannot be empty." else "Password must be at least 6 characters."
            canProceed = false
        } else {
            isPasswordError = false
        }

        if (confirmPassword.isEmpty() || confirmPassword != password) {
            isConfirmPasswordError = true
            confirmPasswordErrorMessage = if (confirmPassword.isEmpty()) "Confirm password cannot be empty." else "Passwords do not match."
            canProceed = false
        } else {
            isConfirmPasswordError = false
        }

        if (fullName.isBlank()) {
            // Consider adding a specific error state for fullName if desired
            // For now, if other fields are valid, but full name is blank, we block proceeding.
            if (canProceed) { // Only mark as error if other fields were fine
                // TODO: Add visual error for full name if desired (e.g., another isError/errorMessage pair)
            }
            canProceed = false
        }
        return canProceed
    }

    LaunchedEffect(isPressed) {
        if (isPressed && !isLoading) { // Only start hold if not already loading and pressed
            showHoldIndicator = true
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < holdDurationMillis) {
                holdProgress = (System.currentTimeMillis() - startTime).toFloat() / holdDurationMillis
                delay(16) // Update roughly 60 times per second
            }
            if (System.currentTimeMillis() - startTime >= holdDurationMillis) {
                if (validateFields()) {
                    onSignUpClick(fullName, email, password, confirmPassword)
                } else {
                    // Validation failed, reset progress and indicator
                    // Errors will be shown by the validation logic setting isError states
                }
            }
            // Reset after press is released or action is triggered/failed
            showHoldIndicator = false
            holdProgress = 0f
        } else {
            // Not pressed or was loading, ensure indicators are reset
            showHoldIndicator = false
            holdProgress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MealFlow",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Create an Account",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    isEmailError = true
                    emailErrorMessage = "Please enter a valid email."
                } else {
                    isEmailError = false
                    emailErrorMessage = ""
                }
            },
            label = { Text("Email") },
            isError = isEmailError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isEmailError) {
            Text(
                text = emailErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(if (isEmailError) 8.dp else 16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (it.isNotEmpty() && it.length < 6) {
                    isPasswordError = true
                    passwordErrorMessage = "Password must be at least 6 characters."
                } else {
                    isPasswordError = false
                    passwordErrorMessage = ""
                }
                if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                    isConfirmPasswordError = true
                    confirmPasswordErrorMessage = "Passwords do not match."
                } else if (confirmPassword.isNotEmpty()) {
                    isConfirmPasswordError = false
                    confirmPasswordErrorMessage = ""
                }
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            },
            isError = isPasswordError,
            modifier = Modifier.fillMaxWidth()
        )
        if (isPasswordError) {
            Text(
                text = passwordErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(if (isPasswordError) 8.dp else 16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (it.isNotEmpty() && it != password) {
                    isConfirmPasswordError = true
                    confirmPasswordErrorMessage = "Passwords do not match."
                } else {
                    isConfirmPasswordError = false
                    confirmPasswordErrorMessage = ""
                }
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                // Consider if a long press on the button should still be the primary action
            }),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password")
                }
            },
            isError = isConfirmPasswordError,
            modifier = Modifier.fillMaxWidth()
        )
        if (isConfirmPasswordError) {
            Text(
                text = confirmPasswordErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(if (isConfirmPasswordError) 24.dp else 32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    // Standard click does nothing or could show a "Hold to submit" hint
                    // For now, it's disabled in favor of the LaunchedEffect handling the press.
                },
                enabled = !isLoading, // Button itself is enabled/disabled by isLoading
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource // Crucial for tracking press state
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else if (showHoldIndicator) {
                    // Show text and progress
                    Box(contentAlignment = Alignment.Center) {
                        Text("Hold to Sign Up...") // You might want to adjust text or show only progress
                        CircularProgressIndicator(
                            progress = holdProgress,
                            modifier = Modifier.size(30.dp), // Slightly larger to be visible around text
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    Text("Sign Up")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { if (!isLoading) onLoginClick() }, enabled = !isLoading) {
            Text("Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "By creating an account, you agree to our Terms & Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Preview remains largely the same, but the hold interaction won't fully work in Preview.
@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")@Composable
fun SignUpScreenPreview() {
    MealFlowTheme {
        var isLoading by remember { mutableStateOf(false) }
        SignUpScreen(
            onSignUpClick = { _, _, _, _ -> isLoading = !isLoading },
            onLoginClick = {},
            isLoading = isLoading
        )
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")@Composable
fun SignUpScreenWithErrorPreview() {
    MealFlowTheme {
        SignUpScreen(
            onSignUpClick = { _, _, _, _ -> },
            onLoginClick = {},
            isLoading = false
        )
    }
}
