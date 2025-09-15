
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.gms.google.services)
    kotlin("plugin.serialization") version "1.9.23" // e.g., 1.9.23
}
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.fatokifury.meal_flow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fatokifury.meal_flow"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("GEMINI_API_KEY") ?: project.findProperty("GEMINI_API_KEY")}\"")
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
        unitTests.all { test -> // 'test' is the Test task
            test.useJUnitPlatform()
        }
    }
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            force("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
            force("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.3")
            force(libs.kotlin.stdlib)
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
                    useVersion(libs.versions.kotlin.get())
                    because("Enforcing consistent Kotlin version for stdlib")
                }
            }
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)

    // ========== NAVIGATION ==========
    implementation(libs.androidx.navigation.compose)

    // ========== DEPENDENCY INJECTION ==========
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)

    // ========== DATABASE ==========
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ========== FIREBASE ==========
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // ========== COROUTINES ==========
    implementation(libs.bundles.kotlinxCoroutines)
    // Force specific versions to resolve conflicts
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ========== NETWORKING ==========
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // ========== SERIALIZATION ==========
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")

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
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

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
}