package com.dripin.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dripin.app.MainActivity

const val RecommendationChannelId = "daily_recommendation"

interface RecommendationNotifier {
    fun showDailyRecommendation(count: Int): NotificationPostResult
}

sealed interface NotificationPostResult {
    data object Posted : NotificationPostResult

    data class Blocked(
        val issue: NotificationCapabilityIssue,
    ) : NotificationPostResult

    data class Failed(
        val reason: String?,
    ) : NotificationPostResult
}

class AndroidRecommendationNotifier(
    private val context: Context,
    private val capabilityReader: NotificationCapabilityReader = AndroidNotificationCapabilityReader(context),
) : RecommendationNotifier {
    override fun showDailyRecommendation(count: Int): NotificationPostResult {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context,
                PostNotificationsPermission,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return NotificationPostResult.Blocked(NotificationCapabilityIssue.RuntimePermissionDenied)
        }

        val capability = capabilityReader.read()
        capability.primaryIssue()?.let { issue ->
            Log.w(
                Tag,
                "Skipping daily recommendation notification: $issue " +
                    "(permission=${capability.runtimePermissionGranted}, " +
                    "appEnabled=${capability.appNotificationsEnabled}, " +
                    "channelExists=${capability.channelExists}, " +
                    "channelBlocked=${capability.channelBlocked})",
            )
            return NotificationPostResult.Blocked(issue)
        }

        ensureChannel()
        return runCatching {
            NotificationManagerCompat.from(context).notify(
                NotificationId,
                NotificationCompat.Builder(context, RecommendationChannelId)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("今日推荐已准备好")
                    .setContentText("今天为你挑了 $count 条稍后再看的内容。")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(buildPendingIntent())
                    .build(),
            )
            NotificationPostResult.Posted
        }.getOrElse { throwable ->
            if (throwable is SecurityException) {
                Log.w(Tag, "Notification permission check became stale before notify()", throwable)
                return NotificationPostResult.Blocked(NotificationCapabilityIssue.RuntimePermissionDenied)
            }
            Log.w(Tag, "Failed to post daily recommendation notification", throwable)
            NotificationPostResult.Failed(
                reason = throwable.message,
            )
        }
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            RecommendationChannelId,
            "每日推荐",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "每天提醒你回看稍后阅读列表。"
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "dripin://today".toUri()
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val NotificationId = 4001
        private const val Tag = "DailyRecommendation"
    }
}
