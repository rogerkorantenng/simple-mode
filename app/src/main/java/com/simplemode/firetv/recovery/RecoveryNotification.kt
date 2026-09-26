package com.simplemode.firetv.recovery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import com.simplemode.firetv.MainActivity

/**
 * The foreground-service notification [RecoveryOverlayService] must post
 * to keep drawing its overlay. Split out purely to keep the service file
 * itself down to lifecycle calls.
 */
object RecoveryNotification {
    private const val CHANNEL_ID = "simple_mode_recovery"

    fun build(service: Service): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = service.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Simple Mode recovery button",
                    NotificationManager.IMPORTANCE_MIN,
                ),
            )
        }

        val contentIntent = PendingIntent.getActivity(
            service,
            0,
            Intent(service, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(service, CHANNEL_ID)
            .setContentTitle("Simple Mode is watching")
            .setContentText("One button always gets back to the simple screen.")
            .setSmallIcon(android.R.drawable.ic_menu_revert)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }
}
