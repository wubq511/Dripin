package com.dripin.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.dripin.app.MainActivity

interface RecommendationNotifier {
    fun showDailyRecommendation(count: Int)
}

class AndroidRecommendationNotifier(
    private val context: Context,
) : RecommendationNotifier {
    override fun showDailyRecommendation(count: Int) {
        ensureChannel()
        NotificationManagerCompat.from(context).notify(
            NotificationId,
            NotificationCompat.Builder(context, ChannelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("今日推荐已准备好")
                .setContentText("今天为你挑了 $count 条稍后再看的内容。")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(buildPendingIntent())
                .build(),
        )
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ChannelId,
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
        const val ChannelId = "daily_recommendation"
        private const val NotificationId = 4001
    }
}
