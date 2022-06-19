package com.tonynowater.mi_notification_intercepter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tonynowater.mi_notification_intercepter.ui.main.MainViewModel
import com.tonynowater.mi_notification_intercepter.ui.main.view.MainScaffold
import com.tonynowater.mi_notification_intercepter.ui.theme.MinotificationIntercepterTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinotificationIntercepterTheme {
                MainScaffold(
                    switchState = viewModel.switchState,
                    notificationPermissionGrantedState = viewModel.notificationPermissionGranted,
                    viewModel = viewModel
                )
            }
        }
    }
}