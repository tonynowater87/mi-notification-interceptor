package com.tonynowater.mi_notification_intercepter.ui.main.view

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.tonynowater.mi_notification_intercepter.ui.main.MainViewModel
import com.tonynowater.mi_notification_intercepter.ui.model.AlertType
import com.tonynowater.mi_notification_intercepter.ui.model.EventModel
import java.text.DateFormat

@Composable
fun MainScaffold(
    switchState: Boolean,
    notificationPermissionGrantedState: Boolean,
    viewModel: MainViewModel? = null
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("通知欄截器") })
        }
    ) {
        Column {
            MainView(
                notificationPermissionGrantedState = notificationPermissionGrantedState,
                serviceRunningState = switchState,
                onCheckedChange = { viewModel!!.switchChanged(it) },
                onClickTestButton = {
                    viewModel!!.pullMessage()
                }
            )

            Divider(modifier = Modifier.fillMaxWidth())

            NotificationList(data = viewModel!!.events)
        }
    }
}


@Composable
fun MainView(
    notificationPermissionGrantedState: Boolean,
    serviceRunningState: Boolean,
    onCheckedChange: (boolean: Boolean) -> Unit,
    onClickTestButton: (() -> Unit)? = null
) {

    val context = LocalContext.current //

    Column(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Spacer(modifier = Modifier.size(10.dp))
        Row {
            Text(text = "通知存取權限是否已授與？")
            Switch(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    },
                checked = notificationPermissionGrantedState,
                onCheckedChange = null
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row {
            Text(text = "通知欄截服務是否啟動？")
            Switch(
                modifier = Modifier.wrapContentSize(),
                checked = serviceRunningState,
                onCheckedChange = onCheckedChange
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Button(onClick = { onClickTestButton?.invoke() }) {
            Text(text = "測試按鈕")
        }
    }
}

@Composable
fun NotificationList(data: List<EventModel>) {
    // TODO animation . . .

    Text(modifier = Modifier.padding(5.dp), text = "阿嬤動態偵測列表", fontWeight = FontWeight.Bold)

    LazyColumn(
        modifier = Modifier
            .padding(5.dp)
    ) {
        items(data) { item ->
            Column {
                Text(
                    text = DateFormat.getDateTimeInstance()
                        .format(item.timestamp.toDate()),
                )
                Text(
                    text = item.type.name,
                    maxLines = 5,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScaffold(
        notificationPermissionGrantedState = true, switchState = false
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationListPreview() {
    Column {
        NotificationList(
            data = listOf(
                EventModel(
                    type = AlertType.OpenRoomDoor,
                    timestamp = Timestamp.now()
                )
            )
        )
    }
}