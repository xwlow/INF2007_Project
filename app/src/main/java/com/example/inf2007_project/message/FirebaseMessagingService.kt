package com.example.inf2007_project.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.inf2007_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            // Handle the message
            sendNotification(
                remoteMessage.data["title"] ?: "New Message",
                remoteMessage.data["body"] ?: "You have a new message"
            )
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "message_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.company_logo)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val channel = NotificationChannel(
            channelId,
            "Message Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send this token to your server
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            FirebaseFirestore.getInstance()
                .collection("userDetail")
                .document(user.uid)
                .update("fcmToken", token)
        }
    }
}