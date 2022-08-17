package com.maf.lmt

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MasterActivity : AppCompatActivity() {
    private val TAG = "zBug" + javaClass.simpleName

    private val handler = Handler.createAsync(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master)

        window.attributes = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

        val editTextNumberInput : EditText = findViewById(R.id.editTextNumber)

        val button : Button = findViewById(R.id.buttonMaster)
        val textView : TextView = findViewById(R.id.textView)

        var rethinkTime = 15
        runnable = Runnable {
            button.text = "GO ($rethinkTime)"
            if (rethinkTime > 0) {
                handler.postDelayed(runnable, 1000)
                --rethinkTime
            }
            else {
                button.text = "GO"
                button.isClickable = true
            }
        }

        button.setOnClickListener {
            Log.i(TAG, "onCreate: button2 clicked")
            if(App.isSnoozed) return@setOnClickListener
            if (editTextNumberInput.text.isBlank()) return@setOnClickListener
            val missionTime:Long = editTextNumberInput.text.toString().toLong()
            if (missionTime < 1) return@setOnClickListener
            if (missionTime > 3 && rethinkTime > 0) {
                button.isClickable = false
                textView.text = "Do you really need so much time?\nThink Again"
                handler.post(runnable)
                return@setOnClickListener
            }
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
            super.onBackPressed()

//            Handler.createAsync(Looper.getMainLooper()).post {
//                moveTaskToBack(true)
//            }
        }
    }

    private fun showPurposeNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = Notification
            .Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("Timer active")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    private fun timeUpAction() {
        val powerManager = getSystemService(PowerManager::class.java)
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake:lock")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        App.snoozeScheduler = Executors.newSingleThreadScheduledExecutor()
        App.snoozeScheduler.schedule({
            Log.i(TAG, "onCreate: coolDown timeout")
            App.isSnoozed = false
            sendBroadcast(Intent(Constants.ACTION_SNOOZE))
            getSystemService(NotificationManager::class.java).cancel(Constants.NOTIFICATION_ID)
            if(wakeLock.isHeld) wakeLock.release()
        },9,TimeUnit.MINUTES)
        App.isSnoozed = true
        sendBroadcast(Intent(Constants.ACTION_SNOOZE))
        Log.i(TAG, "timeUpAction: " + App.isSnoozed)
    }

    private fun showTimesUpNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = Notification
            .Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("Times Up")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}