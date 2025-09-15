package com.fatokifury.meal_flow.ui.viewmodels

import kotlinx.coroutines.tasks.await // If not already there for auth calls
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed Interface to represent different states of an authentication attempt
sealed interface AuthResultState {
    object Idle : AuthResultState
    object Loading : AuthResultState
    data class Success(val uid: String) :
        AuthResultState // Include UID for navigation or further actions

    data class Error(val message: String) : AuthResultState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResultState>(AuthResultState.Idle)
    val authState: StateFlow<AuthResultState> = _authState.asStateFlow()



    fun signUpUser(fullName: String, email: String, pass: String) {
        viewModelScope.launch {
            println("VM: signUpUser coroutine STARTED.")
            _authState.value = AuthResultState.Loading
            try {
                // Basic validation
                if (fullName.isBlank()) {
                    _authState.value = AuthResultState.Error("Full name cannot be empty.")
                    return@launch
                }
                if (email.isBlank() || pass.isBlank()) {
                    _authState.value = AuthResultState.Error("Email and password cannot be empty.")
                    return@launch
                }
                if (pass.length < 6) {
                    _authState.value =
                        AuthResultState.Error("Password must be at least 6 characters.")
                    return@launch
                }
                println("VM: Validations passed (if any).")
                println("VM: Calling createUserWithEmailAndPassword(...).await()")

                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                println("VM: createUserWithEmailAndPassword(...).await() COMPLETED.")
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    println("VM: Firebase user is NOT NULL (uid: ${firebaseUser.uid}).")
                    // Create a user profile document in Firestore
                    val userProfile = hashMapOf(
                        "uid" to firebaseUser.uid,
                        "fullName" to fullName,
                        "email" to firebaseUser.email, // Use email from the created user
                        "createdAt" to com.google.firebase.Timestamp.now() // Optional: record creation time
                    )
                    println("VM: User profile created: $userProfile")

                    println("VM: Calling firestore.collection.document.set(...).await()")
                    // Save to Firestore in a 'users' collection, document ID is user's UID
                    firestore.collection("users").document(firebaseUser.uid)
                        .set(userProfile)
                        .await() // Wait for Firestore operation to complete
                    println("VM: firestore.collection.document.set(...).await() COMPLETED.")


                    _authState.value = AuthResultState.Success(firebaseUser.uid)
                    println("VM: Emitted AuthResultState.Success.")
                } else {
                    println("VM: Firebase user IS NULL after creation attempt.")
                    _authState.value = AuthResultState.Error("Sign up failed. Please try again.")
                    println("VM: Emitted AuthResultState.Error (user null).")
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                println("VM: EXCEPTION in signUpUser coroutine: ${e.message}")
                _authState.value =
                    AuthResultState.Error("An account already exists with this email address.")
                println("VM: Emitted AuthResultState.Error (exception).")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthResultState.Error("Invalid email format.")
                println("VM: signUpUser coroutine FINISHED (finally block).")
            } catch (e: Exception) { // Catch other exceptions, including potential Firestore errors
                _authState.value =
                    AuthResultState.Error(e.message ?: "An unknown error occurred during sign up.")
            }
            println("VM: signUpUser METHOD CALL FINISHED (outer function).") // This will print before the coroutine body usually.
        }
    }


    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthResultState.Loading
            try {
                if (email.isBlank() || pass.isBlank()) {
                    _authState.value = AuthResultState.Error("Email and password cannot be empty.")
                    return@launch
                }

                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val user = result.user
                if (user != null) {
                    _authState.value = AuthResultState.Success(user.uid)
                } else {
                    _authState.value = AuthResultState.Error("Login failed. Please try again.")
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value =
                    AuthResultState.Error("No account found with this email address.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthResultState.Error("Incorrect password. Please try again.")
            } catch (e: Exception) {
                _authState.value =
                    AuthResultState.Error(e.message ?: "An unknown error occurred during login.")
            }
        }
    }

    // Function to reset the auth state, e.g., after an error message has been shown
    fun resetAuthState() {
        _authState.value = AuthResultState.Idle
    }

    // TODO:
    // - Implement User login state observation (e.g., auth.authStateFlow()) to automatically update UI
    // - Sign in with Google
    // - Sign out
    // - Link user to a family or create one (this would involve Firestore interaction with the UID)
}