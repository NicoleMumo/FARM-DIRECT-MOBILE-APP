plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}


android {
    namespace = "com.example.farmdirect" // FIX: Added '='
    compileSdk = 36                  // Updated to 36 to match dependency requirements

    defaultConfig {
        applicationId = "com.example.farmdirect" // Also ensure '=' is used here
        minSdk = 24
        targetSdk = 36  // Updated to 36 to match compileSdk
        versionCode = 1
        versionName = "1.0"

        // enable ViewBinding and Compose
        buildFeatures {
            viewBinding = true // FIX: Added '='
            buildConfig = true // FIX: Added '='
            compose = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // FIX: Added '='
        targetCompatibility = JavaVersion.VERSION_17 // FIX: Added '='
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // With Kotlin 2.0+, Compose compiler is integrated into Kotlin
    // No need for kotlinCompilerExtensionVersion
}

dependencies {
    // Make sure you have the Firebase BOM imported
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Use the latest version

    // Add the dependency for Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database") // Realtime Database for orders

    // Other dependencies...
    // ... other dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
// For other potential missing dependencies
    implementation("com.google.android.material:material:1.12.0")
// **Add these for App Check**
    implementation("com.google.firebase:firebase-appcheck-playintegrity:19.0.1")
    implementation("com.google.firebase:firebase-appcheck-debug") // For testing on emulators/debug builds

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Core KTX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // RecyclerView (for traditional Android views)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ConstraintLayout (for XML layouts)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

}


