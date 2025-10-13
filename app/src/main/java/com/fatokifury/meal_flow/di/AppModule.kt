package com.fatokifury.meal_flow.di

import com.fatokifury.meal_flow.data.AuthRepository
import com.fatokifury.meal_flow.data.AuthRepositoryImpl
import com.fatokifury.meal_flow.data.RecipeRepository
import com.fatokifury.meal_flow.data.RecipeRepositoryImpl
import com.fatokifury.meal_flow.navigation.NavigationService
import com.fatokifury.meal_flow.navigation.NavigationServiceImpl
import com.google.firebase.auth.FirebaseAuth // <-- Import this
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindNavigationService(impl: NavigationServiceImpl): NavigationService

    companion object {

        // V-- THIS IS THE FIX --V
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
        // ^-- THIS IS THE FIX --^

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
    }
}
