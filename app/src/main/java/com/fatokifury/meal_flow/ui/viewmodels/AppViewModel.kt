package com.fatokifury.meal_flow.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.fatokifury.meal_flow.navigation.NavigationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val navigationService: NavigationService
) : ViewModel()
