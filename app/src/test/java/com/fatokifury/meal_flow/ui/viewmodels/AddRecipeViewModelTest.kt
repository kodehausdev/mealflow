package com.fatokifury.meal_flow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.model.Ingredient
import com.fatokifury.meal_flow.model.Recipe
import com.fatokifury.meal_flow.navigation.NavigationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

@OptIn(ExperimentalCoroutinesApi::class)
class AddRecipeViewModelTest {

    // These are the "Fakes" or "Mocks" of our ViewModel's dependencies.
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var navigationService: NavigationService
    private lateinit var application: Application
    private lateinit var savedStateHandle: SavedStateHandle

    // The ViewModel we are going to test
    private lateinit var viewModel: AddRecipeViewModel

    // This dispatcher is for controlling coroutines in tests
    private val testDispatcher = StandardTestDispatcher()

    // This function runs BEFORE every single test
    @BeforeEach
    fun setUp() {
        // Set the main dispatcher to our test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Create the fake dependencies using mockk
        recipeRepository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        navigationService = mockk(relaxed = true)
        application = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle() // For tests, a simple instance is enough

        // Mock the FirebaseAuth to return a fake user with a UID
        val fakeFirebaseUser: FirebaseUser = mockk()
        every { fakeFirebaseUser.uid } returns "test_user_123"
        every { auth.currentUser } returns fakeFirebaseUser

        // Create the actual ViewModel instance, injecting our fakes
        viewModel = AddRecipeViewModel(
            recipeRepository,
            auth,
            navigationService,
            application,
            savedStateHandle
        )
    }

    // This function runs AFTER every single test
    @AfterEach
    fun tearDown() {
        // Reset the main dispatcher to avoid tests interfering with each other
        Dispatchers.resetMain()
    }

    // --- THE ACTUAL TEST CASE ---
    @Test
    fun `saveRecipe WHEN servings are changed THEN ingredients are scaled correctly`() = runTest {
        // --- ARRANGE ---
        // 1. Define the original recipe that we will "load" into the ViewModel.
        val originalIngredient = Ingredient(name = "Flour", quantity = 100.0, unit = "g")
        val originalRecipe = Recipe(
            id = "recipe1",
            title = "Test Cake",
            servings = 4, // Original servings: 4
            ingredients = listOf(originalIngredient)
        )

        // 2. Tell our fake ViewModel to hold this recipe in its state.
        // This simulates loading a recipe for editing.
        viewModel.setInitialStateForTest(originalRecipe)
        viewModel.onTitleChange(originalRecipe.title)
        viewModel.onServingsChange(originalRecipe.servings.toString())
        // Manually set the originalRecipe state since loadRecipeForEditing is private
        // and we want to test the public API. We can also directly update the state for this test.
        AddRecipeUiState(
            recipeId = originalRecipe.id,
            title = originalRecipe.title,
            servings = originalRecipe.servings.toString(),
            ingredients = originalRecipe.ingredients,
            originalRecipe = originalRecipe // Crucial for the scaling logic
        )
        // A bit of reflection or a helper function could set this private state,
        // but for now, let's test the public functions.
        // Let's assume the user changes the servings.

        // --- ACT ---
        // 3. The user changes the servings from "4" to "8".
        viewModel.onServingsChange("8")

        // 4. The user hits the save button.
        viewModel.saveRecipe()

        // Advance the coroutines to make sure the save logic runs
        testDispatcher.scheduler.advanceUntilIdle()

        // --- ASSERT ---
        // 5. We now verify that the `saveRecipe` function in our FAKE repository
        //    was called with the CORRECTLY scaled recipe.
        coVerify {
            recipeRepository.saveRecipe(
                withArg { savedRecipe ->
                    // Check that the servings were updated
                    Assertions.assertEquals(8, savedRecipe.servings)
                    // Check that the ingredient list is not empty
                    Assertions.assertEquals(1, savedRecipe.ingredients.size)
                    // THE MOST IMPORTANT CHECK: Verify the quantity was scaled from 100g to 200g.
                    Assertions.assertEquals(200.0, savedRecipe.ingredients[0].quantity)
                }
            )
        }
    }
}
