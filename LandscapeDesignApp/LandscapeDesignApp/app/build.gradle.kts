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
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    // إعداد النسختين (Flavors) لدعم جوجل وهواوي من نفس الكود
    flavorDimensions.add("platform")
    productFlavors {
        create("google") {
            dimension = "platform"
            applicationIdSuffix = ".google"
            resValue("string", "app_name", "تصميم الحدائق (Google)")
        }
        create("huawei") {
            dimension = "platform"
            applicationIdSuffix = ".huawei"
            resValue("string", "app_name", "تصميم الحدائق (Huawei)")
        }
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // مكتبة SceneView المشتركة
    implementation("io.github.sceneview:arsceneview:2.2.1")

    // تفعيل ARCore حصرياً لنسخة جوجل
    "googleImplementation"("com.google.ar:core:1.44.0")

    // تفعيل Huawei AR Engine حصرياً لنسخة هواوي
    "huaweiImplementation"("com.huawei.hms:arengine:3.18.0.300")

    // Jetpack Compose & Lifecycle BOM
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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
