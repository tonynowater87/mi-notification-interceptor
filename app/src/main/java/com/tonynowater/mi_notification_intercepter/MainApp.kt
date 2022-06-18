package com.tonynowater.mi_notification_intercepter

import android.app.Application

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        private val TAG = MainApp::class.simpleName
    }
}