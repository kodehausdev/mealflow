package com.fatokifury.meal_flow.ui.viewmodels

import android.util.Patterns
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.password
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatokifury.meal_flow.data.AuthRepository
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.Screen
import com.fatokifury.meal_flow.ui.screens.LoginUiState
import com.fatokifury.meal_flow.ui.screens.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthResultEvent {
    data class Success(val message: String) : AuthResultEvent()
    data class Error(val message: String) : AuthResultEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val navigationService: NavigationService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState = _loginUiState.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState = _signUpUiState.asStateFlow()

    private val _authEvents = Channel<AuthResultEvent>()
    val authEvents = _authEvents.receiveAsFlow()

    private var holdJob: Job? = null
    private val holdDurationMillis = 1500L

    // --- Navigation --- //
    fun navigateToLogin() = navigationService.navigateAndPopUp(Screen.Login.route, Screen.SignUp.route)
    fun navigateToSignUp() = navigationService.navigateAndPopUp(Screen.SignUp.route, Screen.Login.route)


    // --- Login Logic --- //
    fun onLoginEmailChange(email: String) {
        _loginUiState.update { it.copy(email = email) }
    }

    fun onLoginPasswordChange(password: String) {
        _loginUiState.update { it.copy(pass = password) } // 'pass' matches LoginUiState
    }

    fun onLoginTogglePasswordVisibility() {
        _loginUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun loginUser() {
        val state = _loginUiState.value
        if (!validateEmail(state.email) || state.pass.isEmpty()) {
            _loginUiState.update { it.copy(isEmailError = !validateEmail(state.email)) }
            return
        }
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true) }
            val result = authRepository.login(state.email, state.pass)
            result.onSuccess {
                _authEvents.send(AuthResultEvent.Success("Login Successful!"))
                navigationService.navigateAndPopUp(Screen.MealList.route, Screen.Login.route)
            }.onFailure { exception ->
                _authEvents.send(AuthResultEvent.Error(exception.message ?: "Unknown login error"))
            }
            _loginUiState.update { it.copy(isLoading = false) }
        }
    }

    // --- Sign Up Logic (Upgraded) --- //

    fun onSignUpFullNameChange(name: String) {
        _signUpUiState.update { it.copy(fullName = name) }
    }

    fun onSignUpEmailChange(email: String) {
        _signUpUiState.update { it.copy(email = email, isEmailError = !validateEmail(email) && email.isNotEmpty()) }
    }

    fun onSignUpPasswordChange(password: String) {
        _signUpUiState.update {
            it.copy(
                password = password,
                isPasswordError = password.isNotEmpty() && password.length < 6
            )
        }
    }

    fun onSignUpConfirmPasswordChange(password: String) {
        val state = _signUpUiState.value
        _signUpUiState.update {
            it.copy(
                confirmPassword = password,
                isConfirmPasswordError = password.isNotEmpty() && password != state.password
            )
        }
    }

    fun onSignUpTogglePasswordVisibility() {
        _signUpUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }
    fun onSignUpToggleConfirmPasswordVisibility() {
        _signUpUiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    private fun allSignUpFieldsValid(): Boolean {
        val state = _signUpUiState.value
        val isFullNameValid = state.fullName.isNotBlank()
        val isEmailValid = validateEmail(state.email)
        val isPasswordValid = state.password.length >= 6
        val isConfirmPasswordValid = state.password == state.confirmPassword && state.confirmPassword.isNotBlank()

        val allValid = isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid

        if (!allValid) {
            _signUpUiState.update {
                it.copy(
                    isEmailError = !isEmailValid,
                    isPasswordError = !isPasswordValid,
                    isConfirmPasswordError = !isConfirmPasswordValid
                )
            }
        }
        return allValid
    }

    fun onSignUpButtonPress() {
        if (_signUpUiState.value.isSubmitting) return

        holdJob?.cancel()
        holdJob = viewModelScope.launch {
            if (allSignUpFieldsValid()) {
                _signUpUiState.update { it.copy(showHoldIndicator = true) }
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < holdDurationMillis) {
                    val progress = (System.currentTimeMillis() - startTime).toFloat() / holdDurationMillis
                    _signUpUiState.update { it.copy(holdProgress = progress) }
                    delay(16)
                }
                _signUpUiState.update { it.copy(holdProgress = 1f, isSubmitting = true) }
                signUpUser() // Call the actual sign-up function
            } else {
                // Instantly reset if fields are not valid
                resetHoldState()
            }
        }
    }

    fun onSignUpButtonRelease() {
        if (_signUpUiState.value.holdProgress < 1f) {
            holdJob?.cancel()
            resetHoldState()
        }
    }

    private fun signUpUser() {
        val state = _signUpUiState.value
        if (!state.isSubmitting) return

        viewModelScope.launch {
            _signUpUiState.update { it.copy(isLoading = true) }
            val result = authRepository.signUp(state.email, state.password)
            result.onSuccess {
                _authEvents.send(AuthResultEvent.Success("Sign Up Successful!"))
                navigationService.navigateAndPopUp(Screen.MealList.route, Screen.SignUp.route)
            }.onFailure { exception ->
                _authEvents.send(AuthResultEvent.Error(exception.message ?: "Unknown sign up error"))
            }
            _signUpUiState.update { it.copy(isLoading = false) }
            resetHoldState() // Reset state after completion
        }
    }

    private fun resetHoldState() {
        holdJob?.cancel()
        _signUpUiState.update { it.copy(showHoldIndicator = false, holdProgress = 0f, isSubmitting = false) }
    }

    private fun validateEmail(text: String): Boolean {
        if (text.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }
}
