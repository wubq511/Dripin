package com.dripin.app.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DailyRecommendationScheduler(
    private val cancelExisting: () -> Unit,
    private val enqueue: (OneTimeWorkRequest) -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun scheduleNextRun(
        time: LocalTime,
        zoneId: ZoneId,
    ) {
        cancelExisting()

        val now = ZonedDateTime.now(clock.withZone(zoneId))
        var nextRun = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        val delay = Duration.between(now, nextRun)
        val request = OneTimeWorkRequestBuilder<DailyRecommendationWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(WorkTag)
            .build()

        enqueue(request)
    }

    fun cancel() {
        cancelExisting()
    }

    companion object {
        const val UniqueWorkName = "daily_recommendation_generation"
        const val WorkTag = "daily_recommendation"

        fun create(context: Context): DailyRecommendationScheduler {
            val workManager = WorkManager.getInstance(context)
            return DailyRecommendationScheduler(
                cancelExisting = { workManager.cancelUniqueWork(UniqueWorkName) },
                enqueue = { request ->
                    workManager.enqueueUniqueWork(
                        UniqueWorkName,
                        ExistingWorkPolicy.REPLACE,
                        request,
                    )
                },
            )
        }
    }
}
