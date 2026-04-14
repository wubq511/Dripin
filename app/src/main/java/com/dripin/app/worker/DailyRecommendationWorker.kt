package com.dripin.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyRecommendationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = Result.success()
}
