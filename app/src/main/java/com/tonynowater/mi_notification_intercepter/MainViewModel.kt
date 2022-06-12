package com.tonynowater.mi_notification_intercepter

import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import com.tonynowater.mi_notification_intercepter.ui.model.InterceptNotificationItem

class MainViewModel(private val app: Application) : AndroidViewModel(app) {


    var notificationPermissionGranted by mutableStateOf(false)
        private set

    var switchState by mutableStateOf(false)
        private set

    var notificationItems: MutableList<InterceptNotificationItem> by mutableStateOf(mutableListOf())
        private set

    init {
        // check needed permission
        checkNeededPermission()

        // check service is started then update the state
        val activityManager = app.getSystemService<ActivityManager>()
        activityManager?.getRunningServices(Int.MAX_VALUE)?.forEach {
            Log.d(TAG, "running service: ${it.service.className}")
            if (it.service.className == MiInterceptorService::class.qualifiedName) {
                switchState = true
            }
        }
    }

    private fun checkNeededPermission() {
        // get all granted services name split by /
        val value = Settings.Secure.getString(app.contentResolver, "enabled_notification_listeners")
        if (value.contains(MiInterceptorService::class.qualifiedName!!)) {
            Log.d(TAG, "checkNeededPermission: true")
            notificationPermissionGranted = true
        } else {
            Log.d(TAG, "checkNeededPermission: false")
            notificationPermissionGranted = false
        }
    }

    fun switchChanged(switch: Boolean) {
        Log.d(TAG, "switchChanged: $switch")
        switchState = switch
        if (switch) {
            // startService
            app.startService(Intent(app, MiInterceptorService::class.java))
        } else {
            // stopService
            app.stopService(Intent(app, MiInterceptorService::class.java))
        }
    }

    fun receiveNotificationItem(item: InterceptNotificationItem) {
        notificationItems = notificationItems.toMutableList().apply {
            this.add(0, item)
        }
    }

    companion object {
        private var TAG = MainViewModel::class.simpleName
    }

}