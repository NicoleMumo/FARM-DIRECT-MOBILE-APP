package com.example.farmdirect

import android.app.Application
import com.example.farmdirect.BuildConfig // CORRECT: Capital 'B'
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Get an instance of FirebaseAppCheck
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        // Check if the app is running in debug mode
        if (BuildConfig.DEBUG) { // CORRECT: Capital 'B'
            // If it's a debug build, install the Debug provider
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // If it's a release build, install the Play Integrity provider
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
