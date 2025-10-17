package com.fatokifury.meal_flow.di

import com.fatokifury.meal_flow.data.AuthRepository
import com.fatokifury.meal_flow.data.AuthRepositoryImpl
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.data.RecipeRepositoryImpl
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.NavigationServiceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// --- MODULE 1: For providing concrete instances (@Provides) ---
@Module
@InstallIn(SingletonComponent::class)
object ProvidesModule { // Changed name to avoid confusion

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}


// --- MODULE 2: For binding interfaces to implementations (@Binds) ---
@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule { // Changed name to avoid confusion

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindNavigationService(impl: NavigationServiceImpl): NavigationService
}
