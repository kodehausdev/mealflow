//package com.fatokifury.meal_flow.ui.viewmodels
//
//import com.google.firebase.auth.AuthResult
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.FirebaseAuthInvalidUserException
//import com.google.firebase.auth.FirebaseAuthUserCollisionException
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.CollectionReference
//import com.google.firebase.firestore.DocumentReference
//import com.google.firebase.firestore.FirebaseFirestore
//import io.mockk.Runs
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.every
//import io.mockk.just
//import io.mockk.mockk
//import io.mockk.slot
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.Assert
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Nested
//import org.junit.jupiter.api.Test
//import kotlin.time.Duration.Companion.seconds
//
//@ExperimentalCoroutinesApi
//class AuthViewModelTest {
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    // Mocks
//    private lateinit var mockFirebaseAuth: FirebaseAuth
//    private lateinit var mockFirebaseFirestore: FirebaseFirestore
//    private lateinit var mockAuthResult: AuthResult
//    private lateinit var mockFirebaseUser: FirebaseUser
//    private lateinit var mockUsersCollection: CollectionReference
//    private lateinit var mockUserDocument: DocumentReference
//
//    // Class under test
//    private lateinit var authViewModel: AuthViewModel
//
//    @BeforeEach
//    fun setUp() {
//        Dispatchers.setMain(testDispatcher)
//        println("SETUP: Dispatchers.setMain called")
//
//        // Initialize mocks
//        mockFirebaseAuth = mockk(relaxed = true)
//        mockAuthResult = mockk(relaxed = true)
//        mockFirebaseUser = mockk(relaxed = true)
//        mockFirebaseFirestore = mockk(relaxed = true) // Keep this consistent
//        mockUsersCollection = mockk(relaxed = true)
//        mockUserDocument = mockk(relaxed = true)
//
//        // Setup default mock behaviors
//        every { mockFirebaseUser.uid } returns "testUid"
//        every { mockFirebaseUser.email } returns "test@example.com"
//        every { mockAuthResult.user } returns mockFirebaseUser
//        every { mockFirebaseFirestore.collection("users") } returns mockUsersCollection
//        every { mockUsersCollection.document(any()) } returns mockUserDocument
//
//        authViewModel = AuthViewModel(mockFirebaseAuth, mockFirebaseFirestore)
//        println("SETUP: Completed (with consistent naming)")
//    }
//
//    @AfterEach
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `signUpUser with valid details success updates authState to Success`() {
//        runTest(testDispatcher, timeout = 20.seconds) {
//            println("SIGNUP_TEST: Entered runTest block.")
//
//            // Block 1: Variable definitions (Verified)
//            val fullName = "Test User"
//            val email = "test@example.com"
//            val password = "password123"
//            println("SIGNUP_TEST: Variables defined (fullName, email, password).")
//
//            val mockReturnedAuthTask = mockk<com.google.android.gms.tasks.Task<AuthResult>>(relaxed = true)
//            every {
//                mockFirebaseAuth.createUserWithEmailAndPassword(email, password)
//            } returns mockReturnedAuthTask
//            println("SIGNUP_TEST: mockFirebaseAuth.createUserWithEmailAndPassword configured to return mockReturnedAuthTask.")
//
//                // Block 2: Mock Firebase Auth success directly (no Task.await())
//                every {
//                mockFirebaseAuth.createUserWithEmailAndPassword(email, password)
//            } returns mockk<com.google.android.gms.tasks.Task<AuthResult>> {
//                every { isSuccessful } returns true
//                every { result } returns mockAuthResult
//                every { exception } returns null
//                every { isCanceled } returns false  // ADD THIS LINE
//                every { isComplete } returns true   // ADD THIS LINE TOO (likely needed)
//            }
//            println("SIGNUP_TEST: mockFirebaseAuth.createUserWithEmailAndPassword configured directly.")
//
//
//
//
//            // Block 3: Mock Firestore operations
//            val mockFirestoreTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
//            every { mockUserDocument.set(any()) } returns mockFirestoreTask
//            every { mockFirestoreTask.isSuccessful } returns true
//            every { mockFirestoreTask.result } returns null
//            every { mockFirestoreTask.exception } returns null
//            every { mockFirestoreTask.isCanceled } returns false  // ADD THIS
//            every { mockFirestoreTask.isComplete } returns true   // ADD THIS
//            println("SIGNUP_TEST: Firestore mockUserDocument.set() configured.")
////             --- RUN TEST NOW (with assertTrue(true)) ---
////             TODO: You'll need to uncomment and complete mockUserDocument, mockUsersCollection, and their 'every' rules in setUp() for this.
//
////             Block 4: Calling the ViewModel function
//             println("SIGNUP_TEST: Calling authViewModel.signUpUser(...)")
//             authViewModel.signUpUser(fullName, email, password)
//             println("SIGNUP_TEST: authViewModel.signUpUser(...) call completed.")
//            // --- RUN TEST NOW (with assertTrue(true)) ---
//
//            // Block 5: Advancing the dispatcher
//             println("SIGNUP_TEST: Advancing dispatcher...")
//             testDispatcher.scheduler.advanceUntilIdle()
//             println("SIGNUP_TEST: Dispatcher advanced.")
//            // --- RUN TEST NOW (with assertTrue(true)) ---
//
//            // Block 6: Collecting the state
//             println("SIGNUP_TEST: Collecting authState.first()...")
//             val state = authViewModel.authState.first()
//            println("SIGNUP_TEST: Collected authState: $state") // Will show Error(...)            // --- RUN TEST NOW (with assertTrue(true)) ---
//
//            // Block 7: Assertions
//             assertTrue(state is AuthResultState.Success, "State should be Success. Was: ${state::class.simpleName}")
//             assertEquals("testUid", (state as AuthResultState.Success).uid)
//             println("SIGNUP_TEST: Assertions passed.")
//
//            assertTrue(true) // Keep this temporary assertion until all blocks are uncommented
//            println("SIGNUP_TEST: Reached end of currently uncommented blocks.")
//        }
//    }
//
//
//
//        @Test
//        fun testSanity() {
//            assertTrue(true)
//    }
//
//
//    @Test
//    fun `loginUser with valid credentials success updates authState to Success`() =
//        runTest(testDispatcher, timeout = 10.seconds) { // Adjusted timeout
//            println("LOGIN_SUCCESS_TEST: Entered runTest block.")
//
//            val email = "test@example.com"
//            val password = "password123"
//            println("LOGIN_SUCCESS_TEST: Variables defined (email, password).")
//
//            // Mock Firebase Auth signInWithEmailAndPassword to succeed
//            // Using the successful pattern from signUpUser:
//            every {
//                mockFirebaseAuth.signInWithEmailAndPassword(email, password)
//            } returns mockk<com.google.android.gms.tasks.Task<AuthResult>> {
//                every { isSuccessful } returns true
//                every { result } returns mockAuthResult // This mockAuthResult is set up in @BeforeEach
//                every { exception } returns null
//                every { isComplete } returns true
//                every { isCanceled } returns false
//            }
//            println("LOGIN_SUCCESS_TEST: mockFirebaseAuth.signInWithEmailAndPassword configured for success.")
//
//            // Act
//            println("LOGIN_SUCCESS_TEST: Calling authViewModel.loginUser(...)")
//            authViewModel.loginUser(email, password)
//            println("LOGIN_SUCCESS_TEST: authViewModel.loginUser(...) call completed.")
//
//            testDispatcher.scheduler.advanceUntilIdle() // Allow coroutines in ViewModel to run
//            println("LOGIN_SUCCESS_TEST: Dispatcher advanced.")
//
//            // Assert
//            val state = authViewModel.authState.first()
//            println("LOGIN_SUCCESS_TEST: Collected authState: $state")
//
//            assertTrue(
//                state is AuthResultState.Success,
//                "State should be Success. Was: ${state::class.simpleName}"
//            )
//            assertEquals("testUid", (state as AuthResultState.Success).uid)
//            println("LOGIN_SUCCESS_TEST: Assertions passed.")
//        }
//
//    // New test for loginUser - Failure Case (e.g., wrong password)
//    @Test
//    fun `loginUser with invalid credentials updates authState to Error`() =
//        runTest(testDispatcher, timeout = 10.seconds) {
//            println("LOGIN_FAILURE_TEST: Entered runTest block.")
//
//            val email = "test@example.com"
//            val password = "wrongPassword"
//            val expectedErrorMessage = "Invalid email or password." // Or specific exception message
//            println("LOGIN_FAILURE_TEST: Variables defined (email, password, expectedErrorMessage).")
//
//            // Mock Firebase Auth signInWithEmailAndPassword to fail
//            // We'll simulate a FirebaseAuthInvalidCredentialsException for this example
//            val mockException = FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Invalid credentials.")
//            every {
//                mockFirebaseAuth.signInWithEmailAndPassword(email, password)
//            } returns mockk<com.google.android.gms.tasks.Task<AuthResult>> {
//                every { isSuccessful } returns false
//                every { result } returns null // No result on failure
//                every { exception } returns mockException // Set the exception
//                every { isComplete } returns true
//                every { isCanceled } returns false
//            }
//            println("LOGIN_FAILURE_TEST: mockFirebaseAuth.signInWithEmailAndPassword configured for failure with exception.")
//
//            // Act
//            println("LOGIN_FAILURE_TEST: Calling authViewModel.loginUser(...)")
//            authViewModel.loginUser(email, password)
//            println("LOGIN_FAILURE_TEST: authViewModel.loginUser(...) call completed.")
//
//            testDispatcher.scheduler.advanceUntilIdle()
//            println("LOGIN_FAILURE_TEST: Dispatcher advanced.")
//
//            // Assert
//            val state = authViewModel.authState.first()
//            println("LOGIN_FAILURE_TEST: Collected authState: $state")
//
//            assertTrue(
//                state is AuthResultState.Error,
//                "State should be Error. Was: ${state::class.simpleName}"
//            )
//
//            val actualErrorMessage = (state as AuthResultState.Error).message
//            val expectedUserFriendlyMessage = "Incorrect password. Please try again." // <<< This is what your VM actually produces
//
//            assertEquals(
//                expectedUserFriendlyMessage,
//                actualErrorMessage,
//                "Error message did not match the expected user-friendly message."
//            )
//
//            println("LOGIN_FAILURE_TEST: Assertions passed.")
//        }
//}
//
//
//
//
//
//
//
//
//
