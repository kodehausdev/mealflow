//package com.fatokifury.meal_flow.ui.viewmodels
//
//import android.util.Patterns
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import com.fatokifury.meal_flow.ui.screens.LoginUiState
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class LoginViewModel @Inject constructor() : ViewModel() {
//
//    // Expose the UI State. Publicly readable, privately writable.
//    var uiState by mutableStateOf(LoginUiState())
//        private set
//
//    // --- PUBLIC EVENT HANDLERS ---
//
//    fun onEmailChange(email: String) {
//        uiState = uiState.copy(
//            email = email,
//            // Validate email as the user types
//            isEmailError = if (email.isNotEmpty()) !validateEmail(email) else false
//        )
//    }
//
//    fun onPasswordChange(password: String) {
//        uiState = uiState.copy(pass = password)
//    }
//
//    fun onTogglePasswordVisibility() {
//        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
//    }
//
//    // --- PRIVATE BUSINESS LOGIC ---
//
//    private fun validateEmail(text: String): Boolean {
//        return Patterns.EMAIL_ADDRESS.matcher(text).matches()
//    }
//}