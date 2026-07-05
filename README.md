# Farm Direct Mobile App

## Overview

Farm Direct Mobile App is an Android application prototype for a farm-to-consumer marketplace. It supports three user roles:

- **Farmer**: manage products, view orders, analyze performance
- **Consumer**: browse products, place orders, manage cart and profile
- **Admin**: monitor users, manage product listings, and review orders

The project uses Firebase for authentication, Firestore for data storage, and Jetpack Compose for key dashboard screens.

## Key Components

### Core Android Components

- `app/src/main/AndroidManifest.xml` - defines app activities and launcher
- `app/src/main/java/com/example/farmdirect/MyApplication.kt` - initializes Firebase and App Check
- `app/src/main/java/com/example/farmdirect/ui/SplashActivity.kt` - app launch screen
- `app/src/main/java/com/example/farmdirect/ui/LoginActivity.kt` - login flow with Firebase Auth
- `app/src/main/java/com/example/farmdirect/ui/RegisterActivity.kt` - registration and user role creation
- `app/src/main/java/com/example/farmdirect/ui/FarmerDashboardActivity.kt` - farmer dashboard entry
- `app/src/main/java/com/example/farmdirect/ui/ConsumerDashboardActivity.kt` - consumer dashboard entry
- `app/src/main/java/com/example/farmdirect/ui/AdminDashboardActivity.kt` - admin dashboard entry

### Firebase and Data Layer

- `app/src/main/java/com/example/farmdirect/utils/FirebaseUtils.kt` - singleton Firebase utilities for Auth, Firestore, Storage, and Realtime Database
- `app/src/main/java/com/example/farmdirect/data/FarmDirectRepository.kt` - Firestore data access for users, products, and orders
- `app/src/main/java/com/example/farmdirect/models.kt` - app user model
- `app/src/main/java/com/example/farmdirect/model/Product.kt` - product model
- `app/src/main/java/com/example/farmdirect/model/Order.kt` - order model and status

### UI Packages

- `app/src/main/java/com/example/farmdirect/ui/admin` - admin-specific Compose screens and view models
- `app/src/main/java/com/example/farmdirect/ui/consumer` - consumer-specific Compose screens and view models
- `app/src/main/java/com/example/farmdirect/ui/farmer` - farmer-specific Compose screens and view models

### Resources

- `app/src/main/res/layout` - XML layouts for login, register, splash, dashboard, and list items
- `app/src/main/res/values` - app colors, strings, and theme definitions

## Architecture

This app is built using a mixed UI approach:

- XML + View Binding for login/register/splash screens
- Jetpack Compose for dashboard and role-specific screens
- Firebase Authentication for user sign-in/register
- Firestore for user profile, products, and orders
- A repository layer for Firestore interactions

## Firebase Collections

The current app expects these Firestore collections:

- `users` - stores user profiles and roles
- `products` - stores product listings for farmers
- `orders` - stores order records

## User Roles

The app uses a `role` field in `users/{uid}` to determine the dashboard:

- `farmer`
- `consumer`
- `admin`

## Setup

1. Open the project in Android Studio.
2. Ensure `google-services.json` is present in `app/`.
3. Configure your Firebase project with Auth and Firestore.
4. Build the app using Gradle:

```bash
./gradlew clean build
```

5. Install to a device or emulator:

```bash
./gradlew installDebug
```

## Requirements

- Android SDK `compileSdk` 36
- Kotlin `jvmTarget` 17
- Firebase dependencies via BOM
- Jetpack Compose support



## Project Structure

- `app/` - main Android module
- `app/src/main/java/com/example/farmdirect/` - app source code
- `app/src/main/res/` - app resources
- `gradle/` - Gradle wrapper and version catalogs

## Contact

For questions or contributions, open an issue or submit a pull request in this repository.
