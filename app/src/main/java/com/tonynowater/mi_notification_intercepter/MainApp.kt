package com.tonynowater.mi_notification_intercepter

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.setLoggingEnabled(true)
            FirebaseFirestore.getInstance().apply {
                useEmulator("10.0.2.2", 5585)
                firestoreSettings = FirebaseFirestoreSettings.Builder().apply {
                    isPersistenceEnabled = false
                }.build()
            }
            FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5584)
        }
    }

    companion object {
        private val TAG = MainApp::class.simpleName
    }
}