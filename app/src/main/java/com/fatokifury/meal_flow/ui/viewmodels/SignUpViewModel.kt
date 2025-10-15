package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.ui.screens.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(SignUpUiState())
        private set

    private var holdJob: Job? = null
    private val holdDurationMillis = 1500L

    // --- REFACTORED FUNCTIONS ---

    fun onButtonPress() {
        // Don't do anything if a submission is already in progress
        if (uiState.isSubmitting) return

        holdJob?.cancel()
        holdJob = viewModelScope.launch {
            uiState = uiState.copy(showHoldIndicator = true)

            // Loop to animate the progress
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < holdDurationMillis) {
                val progress = (System.currentTimeMillis() - startTime).toFloat() / holdDurationMillis
                uiState = uiState.copy(holdProgress = progress)
                delay(16) // ~60fps
            }

            // --- Logic after hold is complete ---
            uiState = uiState.copy(holdProgress = 1f)

            // Now, validate the fields
            if (allFieldsValid()) {
                // If valid, set isSubmitting to true. The UI will observe this.
                uiState = uiState.copy(isSubmitting = true)
            } else {
                // If not valid, just reset the hold indicator.
                delay(400) // Give user feedback that it failed
                resetHoldState()
            }
        }
    }

    fun onButtonRelease() {
        // If the button is released before the hold is complete, cancel everything.
        if (uiState.holdProgress < 1f) {
            holdJob?.cancel()
            resetHoldState()
        }
        // If hold is complete (progress is 1f), do nothing. Let the submission proceed.
    }

    // This function is now crucial. The UI must call it after handling the submission event.
    fun submissionHandled() {
        resetHoldState()
    }

    private fun resetHoldState() {
        holdJob?.cancel()
        // Reset all states related to the hold and submission action
        uiState = uiState.copy(showHoldIndicator = false, holdProgress = 0f, isSubmitting = false)
    }

    // This function now has a single responsibility: validating data.
    private fun allFieldsValid(): Boolean {
        val isFullNameValid = uiState.fullName.isNotBlank()
        val isEmailValid = validateEmail(uiState.email)
        val isPasswordValid = uiState.password.length >= 6
        val isConfirmPasswordValid = uiState.password == uiState.confirmPassword && uiState.confirmPassword.isNotBlank()

        val allValid = isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid

        // Update the error states for the UI to show feedback
        if (!allValid) {
            uiState = uiState.copy(
                isEmailError = !isEmailValid,
                isPasswordError = !isPasswordValid,
                isConfirmPasswordError = !isConfirmPasswordValid
            )
        }
        return allValid
    }

    // --- UNCHANGED HELPER FUNCTIONS ---

    private fun validateEmail(text: String): Boolean {
        if (text.isEmpty()) return false
        return Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }

    fun onFullNameChange(name: String) {
        uiState = uiState.copy(fullName = name)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
        // Validate on change to provide real-time feedback
        if (email.isNotEmpty()) {
            uiState = uiState.copy(isEmailError = !validateEmail(email))
        }
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(
            password = password,
            // Only show error if user has typed something
            isPasswordError = if (password.isNotEmpty()) password.length < 6 else false
        )
        // Also re-validate the confirmation field if it's not empty
        if (uiState.confirmPassword.isNotEmpty()) {
            onConfirmPasswordChange(uiState.confirmPassword)
        }
    }

    fun onConfirmPasswordChange(password: String) {
        uiState = uiState.copy(
            confirmPassword = password,
            isConfirmPasswordError = password != uiState.password
        )
    }

    fun onTogglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun onToggleConfirmPasswordVisibility() {
        uiState = uiState.copy(isConfirmPasswordVisible = !uiState.isConfirmPasswordVisible)
    }
}
