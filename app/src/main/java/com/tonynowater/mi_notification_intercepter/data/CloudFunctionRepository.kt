package com.tonynowater.mi_notification_intercepter.data

import com.tonynowater.mi_notification_intercepter.ui.model.AlertType
import com.tonynowater.mi_notification_intercepter.ui.model.EventModel
import kotlinx.coroutines.Deferred

interface CloudFunctionRepository {
    fun pushMessage(alertType: AlertType)
    suspend fun pullAllTypeEvents(): List<EventModel>
}