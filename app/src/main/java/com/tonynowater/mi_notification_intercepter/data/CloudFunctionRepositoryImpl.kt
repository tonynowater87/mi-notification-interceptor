package com.tonynowater.mi_notification_intercepter.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.tonynowater.mi_notification_intercepter.ui.model.AlertType
import com.tonynowater.mi_notification_intercepter.ui.model.EventModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudFunctionRepositoryImpl(
    private val firebaseFunctions: FirebaseFunctions,
    private val firebaseFirestore: FirebaseFirestore
) :
    CloudFunctionRepository {

    private val messageQueue: ArrayDeque<AlertType> = ArrayDeque(0)
    private var isPushingMessage = false

    override fun pushMessage(alertType: AlertType) {
        if (!isPushingMessage) {
            isPushingMessage = true
            firebaseFunctions.getHttpsCallable("pushMessage")
                .call(mapOf("type" to alertType.toString()))
                .addOnSuccessListener {
                    Log.d(TAG, "pushMessage Result: ${it.data}, $alertType")
                    isPushingMessage = false
                    checkPendingMessages()
                }
                .addOnFailureListener {
                    Log.d(TAG, "pushMessage Error: $it, $alertType")
                    isPushingMessage = false
                    checkPendingMessages()
                }
        } else {
            Log.d(TAG, "pushMessage add to queue: $alertType")
            messageQueue.addLast(alertType)
        }
    }

    private fun checkPendingMessages() {
        if (messageQueue.isNotEmpty()) {
            pushMessage(messageQueue.removeFirst())
        }
    }

    override suspend fun pullAllTypeEvents(): List<EventModel> {
        Log.d(TAG, "pullMessage")
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                firebaseFirestore.collection("events")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener {
                        continuation.resume(it.documents.map { documentSnapshot ->
                            val type = documentSnapshot.getString("type")!!
                            val timestamp = documentSnapshot.getTimestamp("timestamp")!!
                            EventModel.fromData(type, timestamp).also {
                                Log.d(TAG, "pullMessage Result: $it")
                            }
                        })
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "pullMessage Error: $it")
                        continuation.resumeWithException(it)
                    }
            }
        }
    }

    companion object {
        private val TAG = CloudFunctionRepository::class.simpleName
    }
}