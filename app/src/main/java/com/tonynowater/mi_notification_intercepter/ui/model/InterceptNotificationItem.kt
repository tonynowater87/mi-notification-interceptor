package com.tonynowater.mi_notification_intercepter.ui.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InterceptNotificationItem(
    val title: String,
    val text: String,
    val time: Long
) : Parcelable
