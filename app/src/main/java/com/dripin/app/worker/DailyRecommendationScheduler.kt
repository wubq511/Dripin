package com.dripin.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyRecommendationScheduler(
    private val cancelExisting: () -> Unit,
    private val scheduleAt: (Long) -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) : SchedulerController {
    override fun scheduleNextRun(
        time: LocalTime,
        zoneId: ZoneId,
        catchUpIfDue: Boolean,
    ) {
        cancelExisting()

        val now = ZonedDateTime.now(clock.withZone(zoneId))
        var nextRun = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (!nextRun.isAfter(now)) {
            nextRun = if (catchUpIfDue) now else nextRun.plusDays(1)
        }

        scheduleAt(nextRun.toInstant().toEpochMilli())
    }

    override fun cancel() {
        cancelExisting()
    }

    companion object {
        const val AlarmAction = "com.dripin.app.action.RUN_DAILY_RECOMMENDATION"
        private const val AlarmRequestCode = 4001

        fun create(context: Context): DailyRecommendationScheduler {
            val appContext = context.applicationContext
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return DailyRecommendationScheduler(
                cancelExisting = { alarmManager.cancel(buildAlarmIntent(appContext)) },
                scheduleAt = { triggerAtMillis ->
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        buildAlarmIntent(appContext),
                    )
                },
            )
        }

        private fun buildAlarmIntent(context: Context): PendingIntent {
            val intent = Intent(context, DailyRecommendationAlarmReceiver::class.java).apply {
                action = AlarmAction
            }
            return PendingIntent.getBroadcast(
                context,
                AlarmRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

interface SchedulerController {
    fun scheduleNextRun(
        time: LocalTime,
        zoneId: ZoneId,
        catchUpIfDue: Boolean = false,
    )

    fun cancel()
}
