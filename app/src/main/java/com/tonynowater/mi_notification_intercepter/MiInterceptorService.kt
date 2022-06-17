package com.tonynowater.mi_notification_intercepter

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.tonynowater.mi_notification_intercepter.ui.model.InterceptNotificationItem

class MiInterceptorService : NotificationListenerService() {

    companion object {
        private val TAG = MiInterceptorService::class.simpleName
        val ACTION_NOTIFICATION = "com.tonynowater.action_notification_interceptor"
        val KEY_NOTIFICATION = "com.tonynowater.key_notification_interceptor"
    }

    private lateinit var firebaseFunctions: FirebaseFunctions

    override fun onCreate() {
        super.onCreate()
        firebaseFunctions = FirebaseFunctions.getInstance()
        Log.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "onListenerConnected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "onListenerDisconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val title = sbn.notification.extras.getString("android.title")
        val text = sbn.notification.extras.getString("android.text")

        if (sbn.packageName == "com.xiaomi.smarthome") {
            // TODO save to local db
            when {
                text?.contains("開啟") == true -> {
                    firebaseFunctions.getHttpsCallable("pushMessage")
                        .call(
                            mapOf(
                                "type" to "OpenRoomDoor",
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "pushed OpenRoomDoor")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "pushed OpenRoomDoor failed $it")
                        }
                }
                text?.contains("關閉") == true -> {
                    firebaseFunctions.getHttpsCallable("pushMessage")
                        .call(
                            mapOf(
                                "type" to "CloseRoomDoor",
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "pushed CloseRoomDoor")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "pushed CloseRoomDoor failed $it")
                        }
                }
                text?.contains("逾時") == true -> {
                    // TODO
                }
                text?.contains("移動") == true -> {
                    firebaseFunctions.getHttpsCallable("pushMessage")
                        .call(
                            mapOf(
                                "type" to "UpDownStairs",
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "pushed UpDownStairs")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "pushed UpDownStairs failed $it")
                        }
                }
            }

            Log.d(TAG, "onXiaoMiNotificationPosted: $title $text")
        }
        sendBroadcast(Intent(ACTION_NOTIFICATION).apply {
            putExtra(
                KEY_NOTIFICATION, InterceptNotificationItem(
                    title = title ?: "無標題",
                    text = text ?: "無內容",
                    time = sbn.postTime
                )
            )
        })
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationPosted(sbn, rankingMap)
        Log.d(TAG, "onNotificationPosted: rankingMap: ${rankingMap.orderedKeys.contentToString()}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "onNotificationRemoved: $sbn")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationRemoved(sbn, rankingMap)
        Log.d(TAG, "onNotificationRemoved: $sbn, rankingMap:$rankingMap")
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        Log.d(TAG, "onNotificationRemoved: $sbn, rankingMap:$rankingMap, reason:$reason")
    }
}