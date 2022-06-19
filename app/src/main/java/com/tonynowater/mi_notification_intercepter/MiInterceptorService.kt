package com.tonynowater.mi_notification_intercepter

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.tonynowater.mi_notification_intercepter.data.CloudFunctionRepository
import com.tonynowater.mi_notification_intercepter.data.CloudFunctionRepositoryImpl
import com.tonynowater.mi_notification_intercepter.ui.model.AlertType

class MiInterceptorService : NotificationListenerService() {

    companion object {
        private val TAG = MiInterceptorService::class.simpleName
    }

    private lateinit var cloudFunctionRepository: CloudFunctionRepository

    override fun onCreate() {
        super.onCreate()
        cloudFunctionRepository = CloudFunctionRepositoryImpl(
            firebaseFunctions = FirebaseFunctions.getInstance(),
            firebaseFirestore = FirebaseFirestore.getInstance()
        )
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

        val title = sbn.notification.extras.getString("android.title") // 有人移動-向手機發送通知
        val text = sbn.notification.extras.getString("android.text") // 智慧通知

        if (sbn.packageName == "com.xiaomi.smarthome") {
            // TODO save to local db?
            when {
                title?.contains("開啟") == true -> {
                    cloudFunctionRepository.pushMessage(AlertType.OpenRoomDoor)
                }
                title?.contains("關閉") == true -> {
                    cloudFunctionRepository.pushMessage(AlertType.CloseRoomDoor)
                }
                title?.contains("逾時") == true -> {
                    // TODO
                }
                title?.contains("移動") == true -> {
                    cloudFunctionRepository.pushMessage(AlertType.UpDownStairs)
                }
            }

            Log.d(TAG, "onXiaoMiNotificationPosted: $title $text")
        }
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