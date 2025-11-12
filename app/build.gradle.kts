plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.farmdirect" // FIX: Added '='
    compileSdk = 34                  // FIX: Added '='

    defaultConfig {
        applicationId = "com.example.farmdirect" // Also ensure '=' is used here
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // enable ViewBinding
        buildFeatures {
            viewBinding = true // FIX: Added '='
            buildConfig = true // FIX: Added '='
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
}

dependencies {
    // Make sure you have the Firebase BOM imported
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Use the latest version

    // Add the dependency for Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // You likely need these as well, based on your FirebaseUtils.kt file
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Other dependencies...
    // ... other dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
// For other potential missing dependencies
    implementation("com.google.android.material:material:1.12.0")
// **Add these for App Check**
    implementation("com.google.firebase:firebase-appcheck-playintegrity:19.0.1")
    implementation("com.google.firebase:firebase-appcheck-debug") // For testing on emulators/debug builds

}


