plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.landscapedesign"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.landscapedesign"
        minSdk = 26 // ARCore minimum
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Force Right-To-Left layout by default (Arabic-only app)
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // App supports Arabic only; force the locale and RTL
    androidResources {
        // generateLocaleConfig = true // uncomment if using per-app language API
    }
}

dependencies {
    // ARCore — stable, version-independent raw SDK. Session/Config/Frame/Anchor
    // APIs used across ar/ARSessionManager.kt have not had breaking changes
    // across recent minor releases.
    implementation("com.google.ar:core:1.44.0")

    // SceneView for Jetpack Compose + ARCore + Filament rendering.
    //
    // ⚠ KNOWN API-GENERATION BREAK: `io.github.sceneview:arsceneview` moved
    // from an imperative, instantiable-View API (pre-2.x: `ArSceneView`,
    // lowercase "r", configured via `.apply { onSessionConfiguration = ...;
    // onArSessionFailed = ...; planeRenderer.isVisible = true }`) to a pure
    // Compose-function API in 2.x (`ARSceneView(modifier, planeRenderer,
    // onSessionUpdated) { ... }`, capital "AR"). This version (2.2.1) is the
    // Compose-function generation — see ui/ArCameraPreview.kt, which only
    // relies on the 3 parameters verified directly against the library's
    // public source (github.com/sceneview/sceneview). If this dependency is
    // ever bumped, re-check ui/ArCameraPreview.kt against that version's
    // actual public API before assuming any other parameter names exist.
    implementation("io.github.sceneview:arsceneview:2.2.1")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
