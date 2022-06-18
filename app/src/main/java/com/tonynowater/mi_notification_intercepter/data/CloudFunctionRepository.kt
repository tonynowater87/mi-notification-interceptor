package com.tonynowater.mi_notification_intercepter.data

import com.tonynowater.mi_notification_intercepter.ui.model.AlertType

interface CloudFunctionRepository {
    fun pushMessage(alertType: AlertType)
}