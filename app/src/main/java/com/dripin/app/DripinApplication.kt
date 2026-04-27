package com.dripin.app

import android.app.Application
import com.dripin.app.worker.DailyRecommendationRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DripinApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            DailyRecommendationRuntime.syncSchedule(this@DripinApplication)
        }
    }
}
