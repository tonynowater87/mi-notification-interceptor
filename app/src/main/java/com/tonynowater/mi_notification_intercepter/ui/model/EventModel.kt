package com.tonynowater.mi_notification_intercepter.ui.model

import com.google.firebase.Timestamp

data class EventModel(
    val type: AlertType,
    val timestamp: Timestamp
) {
    companion object {
        fun fromData(alertType: String, timestamp: Timestamp): EventModel {
            return EventModel(
                type = AlertType.valueOf(alertType),
                timestamp = timestamp
            )
        }
    }
}
