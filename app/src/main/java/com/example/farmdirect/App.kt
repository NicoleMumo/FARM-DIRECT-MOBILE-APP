package com.example.farmdirect

import android.app.Application
import android.util.Log
import com.example.farmdirect.BuildConfig // CORRECT: Capital 'B'
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set up global exception handler to catch unhandled crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("FarmDirect", "UNCAUGHT EXCEPTION in thread ${thread.name}", exception)
            println("FarmDirect CRASH: ${exception.message}")
            exception.printStackTrace()
            // Call the default handler
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Get an instance of FirebaseAppCheck
            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            // Check if the app is running in debug mode
            if (BuildConfig.DEBUG) { // CORRECT: Capital 'B'
                // If it's a debug build, install the Debug provider
                val debugFactory = DebugAppCheckProviderFactory.getInstance()
                firebaseAppCheck.installAppCheckProviderFactory(debugFactory)
                
                // Log the debug token for physical devices
                // Check Logcat with filter "FirebaseAppCheck" to see the token
                Log.d("FirebaseAppCheck", "Debug App Check enabled. Check Logcat for debug token.")
            } else {
                // If it's a release build, install the Play Integrity provider
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error initializing Firebase: ${e.message}", e)
        }
    }
}
