package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.udacity.detail.DetailActivity
import timber.log.Timber

private const val NOTIFICATION_ID = 0

//private lateinit var builder: NotificationCompat.Builder

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context,
                                         bundle: Bundle) {

    Timber.i("Notification sent")

    // Create intent to navigate to detail activity
    val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
        this.putExtra("downloadBundle", bundle)
    }
    Timber.i("contentIntent: $contentIntent")

    // Create PendingIntent to allow notification to open detail activity
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT,
        bundle
    )

    Timber.i("contentPendingIntent: $contentPendingIntent")

    // Get an instance of NotificationCompat.Builder
    val builder = NotificationCompat.Builder(
        applicationContext,
        // Use 'download' notification channel
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        // Set title, text and icon to builder
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext
            .getString(R.string.notification_title))
        .setContentText(messageBody)

        // Add content intent action
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            contentPendingIntent
        )

        // Set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    // Call notify
    notify(NOTIFICATION_ID, builder.build())

}

//Extension function to cancel all notifications
fun NotificationManager.cancelNotifications() {
    cancelAll()
}
