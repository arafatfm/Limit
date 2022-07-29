package com.maf.lmt

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MasterActivity : AppCompatActivity() {
    private val TAG = "zBug" + javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master)

        window.attributes = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

        val editTextNumberInput : EditText = findViewById(R.id.editTextNumber)

        val button : Button = findViewById(R.id.buttonMaster)

        button.setOnClickListener {
            Log.i(TAG, "onCreate: button2 clicked")
            if(App.isSnoozed) return@setOnClickListener
            if (editTextNumberInput.text.isBlank()) return@setOnClickListener
            val missionTime:Long = editTextNumberInput.text.toString().toLong()
            if (missionTime < 1) return@setOnClickListener
            App.onMission = true
            sendBroadcast(Intent(Constants.ACTION_STOP_TRACKING))
            App.missionScheduler = Executors.newSingleThreadScheduledExecutor()
            App.missionScheduler.schedule({
                Log.i(TAG, "onCreate: scheduler timeout")
                App.onMission = false
                sendBroadcast(Intent(Constants.ACTION_START_TRACKING))

                showTimesUpNotification()
                timeUpAction()

            },missionTime,TimeUnit.MINUTES)

            showPurposeNotification()

            Handler.createAsync(Looper.getMainLooper()).post {
//                moveTaskToBack(true)
                finish()
            }
        }
    }

    private fun showPurposeNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = Notification
            .Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("Timer active")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        manager.notify(Constants.NOTIFICATION_ID, notification)
    }

    private fun timeUpAction() {
        App.snoozeScheduler = Executors.newSingleThreadScheduledExecutor()
        App.snoozeScheduler.schedule({
            Log.i(TAG, "onCreate: coolDown timeout")
            App.isSnoozed = false
            sendBroadcast(Intent(Constants.ACTION_SNOOZE))
            getSystemService(NotificationManager::class.java).cancel(Constants.NOTIFICATION_ID)
        },10,TimeUnit.MINUTES)
        App.isSnoozed = true
        sendBroadcast(Intent(Constants.ACTION_SNOOZE))
        Log.i(TAG, "timeUpAction: " + App.isSnoozed)
    }

    private fun showTimesUpNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = Notification
            .Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("Times Up")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        manager.notify(Constants.NOTIFICATION_ID, notification)
    }

    override fun onBackPressed() {}
}