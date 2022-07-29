package com.maf.lmt

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

class ForegroundService : Service() {
    private val TAG = "zBug" + javaClass.simpleName

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "onReceive: broadcast received")
            startActivity(Intent(context, MasterActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        App.isForegroundServiceActive = true
        sendBroadcast(Intent(Constants.ACTION_START_TRACKING))
        registerReceiver(receiver, IntentFilter("TRIGGERED"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        return START_REDELIVER_INTENT
    }

    private fun showNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification
            .Builder(this, Constants.FOREGROUND_CHANNEL_ID)
            .setContentTitle(applicationInfo.loadLabel(packageManager).toString() + " standby")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            Constants.FOREGROUND_CHANNEL_ID, "Ongoing",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        val anotherChannel = NotificationChannel(
            Constants.CHANNEL_ID, "HeadsUp",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(anotherChannel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        App.isForegroundServiceActive = false
        sendBroadcast(Intent(Constants.ACTION_STOP_TRACKING))
        App.missionScheduler.shutdownNow()
        unregisterReceiver(receiver)
    }
}