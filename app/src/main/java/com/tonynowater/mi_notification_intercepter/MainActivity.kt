package com.tonynowater.mi_notification_intercepter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tonynowater.mi_notification_intercepter.ui.main.view.MainScaffold
import com.tonynowater.mi_notification_intercepter.ui.model.InterceptNotificationItem
import com.tonynowater.mi_notification_intercepter.ui.theme.MinotificationIntercepterTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val item =
                intent?.getParcelableExtra<InterceptNotificationItem>(MiInterceptorService.KEY_NOTIFICATION)
                    ?: return
            Log.d("[DEBUG]", "onReceive: $item")
            viewModel.receiveNotificationItem(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinotificationIntercepterTheme {
                MainScaffold(
                    switchState = viewModel.switchState,
                    notificationPermissionGrantedState = viewModel.notificationPermissionGranted,
                    items = viewModel.notificationItems,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            notificationReceiver,
            IntentFilter(MiInterceptorService.ACTION_NOTIFICATION)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(notificationReceiver)
    }
}