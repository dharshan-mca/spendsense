package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.util.UpiDetectionManager
import com.example.util.UpiNotificationParser

class UpiNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        try {
            val packageName = sbn.packageName ?: ""
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            val title = extras.getString(Notification.EXTRA_TITLE)
                ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: ""

            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            val combinedBody = buildString {
                append(text)
                if (bigText.isNotEmpty() && bigText != text) {
                    append(" ").append(bigText)
                }
                if (subText.isNotEmpty()) {
                    append(" ").append(subText)
                }
            }.trim()

            val parsed = UpiNotificationParser.parse(packageName, title, combinedBody)
            if (parsed != null) {
                Log.d("UpiNotificationService", "Detected UPI transaction from $packageName: $parsed")
                UpiDetectionManager.onTransactionDetected(applicationContext, parsed)
            }
        } catch (e: Exception) {
            Log.e("UpiNotificationService", "Error processing notification", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("UpiNotificationService", "Notification listener connected successfully")
        UpiDetectionManager.updateServiceConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("UpiNotificationService", "Notification listener disconnected")
        UpiDetectionManager.updateServiceConnected(false)
    }
}
