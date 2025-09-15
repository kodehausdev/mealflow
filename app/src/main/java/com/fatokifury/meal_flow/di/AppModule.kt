package com.fatokifury.meal_flow.di


import com.fatokifury.meal_flow.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

//    @Provides
//    @Singleton
//    fun provideGenerativeModel(): GenerativeModel {
//        return GenerativeModel(
//            modelName = "gemini-pro", // Or your desired model, e.g., "gemini-1.5-flash"
//            apiKey = BuildConfig.GEMINI_API_KEY
//        )
//    }
}
