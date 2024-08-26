package com.example.userblinkitclone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.userblinkitclone.activity.UsersMainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

// jab hum admin ke side se kuch notification send karenge toh tab ye code chalega aur hume notification dekhne ko milega
// agar admin ke side notification chhaiye toh admin ki side ye class hone chahiye

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val channelId = "UserBlinkit"
        val channel = NotificationChannel(channelId, "BlinkIt", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Blinkit messages"
            enableLights(true)   // notification sabse uppar main dikhega
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, UsersMainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(Random.nextInt(), notification)

    }
}