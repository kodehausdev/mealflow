// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}

// In C:/Users/SEYI/AndroidStudioProjects/MealFlow/build.gradle.kts
allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1") // Be explicit
            force("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm:1.8.1") // Be explicit
        }
    }
}