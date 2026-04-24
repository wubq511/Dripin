package com.dripin.app.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

const val PostNotificationsPermission = "android.permission.POST_NOTIFICATIONS"

data class NotificationCapabilitySnapshot(
    val runtimePermissionGranted: Boolean,
    val appNotificationsEnabled: Boolean,
    val channelExists: Boolean,
    val channelBlocked: Boolean,
) {
    val canDeliverNotifications: Boolean
        get() = runtimePermissionGranted && appNotificationsEnabled && !channelBlocked

    fun primaryIssue(): NotificationCapabilityIssue? = when {
        !runtimePermissionGranted -> NotificationCapabilityIssue.RuntimePermissionDenied
        !appNotificationsEnabled -> NotificationCapabilityIssue.AppNotificationsDisabled
        channelBlocked -> NotificationCapabilityIssue.ChannelBlocked
        else -> null
    }
}

enum class NotificationCapabilityIssue {
    RuntimePermissionDenied,
    AppNotificationsDisabled,
    ChannelBlocked,
}

interface NotificationCapabilityReader {
    fun read(): NotificationCapabilitySnapshot
}

class AndroidNotificationCapabilityReader(
    private val context: Context,
    private val channelId: String = RecommendationChannelId,
) : NotificationCapabilityReader {
    override fun read(): NotificationCapabilitySnapshot {
        val appNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val runtimePermissionGranted = if (Build.VERSION.SDK_INT < 33) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                PostNotificationsPermission,
            ) == PackageManager.PERMISSION_GRANTED
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId)
        val channelExists = channel != null
        val channelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE

        return NotificationCapabilitySnapshot(
            runtimePermissionGranted = runtimePermissionGranted,
            appNotificationsEnabled = appNotificationsEnabled,
            channelExists = channelExists,
            channelBlocked = channelBlocked,
        )
    }
}
