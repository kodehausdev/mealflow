package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.fatokifury.meal_flow.model.Meal
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

//@HiltViewModel
class MealViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
    // TODO: Potentially inject a UserRepository or FamilyRepository later
) : ViewModel() {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    // TODO: Implement meal logic:
    // - Fetch meals for the current family from Firestore
    // - Add a new meal to Firestore
    // - Potentially delete or update meals
}
