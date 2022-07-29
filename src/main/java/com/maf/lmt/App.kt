package com.maf.lmt

import android.app.Application
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class App : Application() {
    private val TAG = "zBug" + javaClass.simpleName

    companion object {
        var isAccessibilityServiceConnected = false
        var onMission = false
        var isSnoozed = false
        var isForegroundServiceActive = false
        var missionScheduler : ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        var snoozeScheduler : ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    }
}