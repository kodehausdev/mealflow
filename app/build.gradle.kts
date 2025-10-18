plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.21"
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.fatokifury.meal_flow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fatokifury.meal_flow"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${System.getenv("GEMINI_API_KEY") ?: project.findProperty("GEMINI_API_KEY")}\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }

    // Simplified resolution strategy - remove forced versions that cause conflicts
    configurations.all {
        resolutionStrategy {
            // Force latest foundation version to ensure SnapPositionInLayout is available
            force("androidx.compose.foundation:foundation:1.6.8")
            force("androidx.compose.foundation:foundation-layout:1.6.8")

            // Only force critical version conflicts
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
                    useVersion(libs.versions.kotlin.get())
                    because("Enforcing consistent Kotlin version for stdlib")
                }
            }
        }
    }
}

dependencies {
    // Calendar Library
    implementation(libs.compose.calendar.view)

    // ========== KOTLIN CORE ==========
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)


    // ========== ANDROID CORE ==========
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ========== JETPACK COMPOSE ==========
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.material.icons.extended)


    // ========== NAVIGATION ==========
    implementation(libs.androidx.navigation.compose)

    // ========== DEPENDENCY INJECTION ==========
    implementation(libs.bundles.hilt)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.foundation)
    ksp(libs.hilt.android.compiler)

    // ========== DATABASE ==========
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ========== FIREBASE ==========
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.firebase.storage.ktx)


    // ========== COROUTINES ==========
    implementation(libs.bundles.kotlinxCoroutines)

    // ========== NETWORKING ==========
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // ========== SERIALIZATION ==========
    implementation(libs.kotlinx.serialization.json)

    // ========== IMAGE LOADING ==========
    implementation(libs.coil.compose)

    // ========== HTML PARSING ==========
    implementation(libs.jsoup)

    // ========== AI/ML ==========
    implementation(libs.google.ai.generativeai)

    // ========== TESTING DEPENDENCIES ==========
    // JUnit 5
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Coroutines Testing
    testImplementation(libs.kotlinx.coroutines.test)

    // MockK for mocking
    testImplementation(libs.mockk)

    // Android Architecture Components testing
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.lifecycle.viewmodel.testing)

    // ========== ANDROID TESTING ==========
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // ========== DEBUG DEPENDENCIES ==========
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ...

    // ADD THIS LINE FOR VOLLEY
    implementation(libs.volley)
}
