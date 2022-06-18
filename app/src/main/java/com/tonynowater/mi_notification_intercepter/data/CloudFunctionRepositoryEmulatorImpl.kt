package com.tonynowater.mi_notification_intercepter.data

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.tonynowater.mi_notification_intercepter.ui.model.AlertType

class CloudFunctionRepositoryEmulatorImpl(private val firebaseFunctions: FirebaseFunctions) :
    CloudFunctionRepository {

    init {
        firebaseFunctions.useEmulator("10.0.2.2", 5582)
    }

    override fun pushMessage(alertType: AlertType) {
        firebaseFunctions.getHttpsCallable("pushMessage")
            .call(mapOf("type" to alertType.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "pushMessage Result: ${it.data}")
            }
            .addOnFailureListener {
                Log.d(TAG, "pushMessage Error: ${it}")
            }
    }

    companion object {
        private val TAG = CloudFunctionRepository::class.simpleName
    }
}