package com.tonynowater.mi_notification_intercepter.ui.main

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
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.tonynowater.mi_notification_intercepter.MiInterceptorService
import com.tonynowater.mi_notification_intercepter.data.CloudFunctionRepository
import com.tonynowater.mi_notification_intercepter.data.CloudFunctionRepositoryImpl
import com.tonynowater.mi_notification_intercepter.ui.model.EventModel
import kotlinx.coroutines.launch

class MainViewModel(private val app: Application) : AndroidViewModel(app) {


    var notificationPermissionGranted by mutableStateOf(false)
        private set

    var switchState by mutableStateOf(false)
        private set

    var events: List<EventModel> by mutableStateOf(listOf())
        private set

    private var repository: CloudFunctionRepository = CloudFunctionRepositoryImpl(
        FirebaseFunctions.getInstance(),
        FirebaseFirestore.getInstance()
    )

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

    fun pullMessage() {
        viewModelScope.launch {
            val result = repository.pullAllTypeEvents()
            Log.d(TAG, "getEventResult: $result")
            events = result.toList()
        }
    }

    companion object {
        private var TAG = MainViewModel::class.simpleName
    }

}