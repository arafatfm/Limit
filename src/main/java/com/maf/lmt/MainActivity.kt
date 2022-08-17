package com.maf.lmt

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "zBug" + javaClass.simpleName

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "onReceive: broadcast received")
            val tButtonMain : ToggleButton = findViewById(R.id.toggleButtonMain)
            when(intent?.action) {
                Constants.ACTION_SNOOZE -> {
                    tButtonMain.isEnabled = !App.isSnoozed
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonPermissionSettings : Button = findViewById(R.id.buttonPermissionSettings)
        val buttonStopMission : Button = findViewById(R.id.buttonStopMission)
        val tButtonMain : ToggleButton = findViewById(R.id.toggleButtonMain)
        val tButtonAccessibility : ToggleButton = findViewById(R.id.toggleButtonAccessibility)
        val tButtonOverlay : ToggleButton = findViewById(R.id.toggleButtonOverlay)

//        tButtonMain.isEnabled = !App.isSnoozed
//
//        tButtonAccessibility.isChecked = App.isAccessibilityServiceConnected
        tButtonAccessibility.setOnClickListener {

            if(App.isAccessibilityServiceConnected) {
                tButtonAccessibility.isChecked = true
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                tButtonAccessibility.isChecked = false
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        buttonStopMission.setOnClickListener {
            Log.i(TAG, "Butt clicked")
            buttonStopMission.visibility = View.INVISIBLE
            if (!App.missionScheduler.isShutdown) {
                App.missionScheduler.shutdownNow()
                App.onMission = false
                getSystemService(NotificationManager::class.java).cancel(Constants.NOTIFICATION_ID)
                sendBroadcast(Intent(Constants.ACTION_START_TRACKING))
            }
        }

//        tButtonMain.isChecked = App.isForegroundServiceActive
//        tButtonAccessibility.isEnabled = !tButtonMain.isChecked
//        tButtonOverlay.isEnabled = !tButtonMain.isChecked
//        buttonPermissionSettings.isEnabled = !tButtonMain.isChecked

        tButtonMain.setOnClickListener {
            Log.i(TAG, "Button Clicked")

            val serviceIntent = Intent(this, ForegroundService::class.java)

            if (App.isForegroundServiceActive) {
                tButtonMain.isChecked = false
                buttonStopMission.visibility = View.INVISIBLE
                stopService(serviceIntent)
            } else {
                if (App.isAccessibilityServiceConnected && Settings.canDrawOverlays(this)) {
                    tButtonMain.isChecked = true
                    startForegroundService(serviceIntent)
                }
                else {
                    Toast.makeText(this, "Provide necessary permissions", Toast.LENGTH_SHORT).show()
                    tButtonMain.isChecked = false
                    return@setOnClickListener
                }
            }
            tButtonAccessibility.isEnabled = !tButtonMain.isChecked
            tButtonOverlay.isEnabled = !tButtonMain.isChecked
            buttonPermissionSettings.isEnabled = !tButtonMain.isChecked
        }

        buttonPermissionSettings.setOnClickListener { launchSettings() }

//        tButtonOverlay.isChecked = Settings.canDrawOverlays(this)
        tButtonOverlay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            tButtonOverlay.isChecked = Settings.canDrawOverlays(this)
        }
    }

    private fun launchSettings() {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        intent.putExtra("extra_pkgname", packageName)
        startActivity(intent)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val prefString = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return prefString != null && prefString.contains(
            context.packageName + "/" +
                    MyAccessibilityService::class.java.canonicalName
        )
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Constants.ACTION_SNOOZE)
        registerReceiver(receiver,filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        val buttonPermissionSettings : Button = findViewById(R.id.buttonPermissionSettings)
        val buttonStopMission : Button = findViewById(R.id.buttonStopMission)
        val tButtonMain : ToggleButton = findViewById(R.id.toggleButtonMain)
        val tButtonAccessibility : ToggleButton = findViewById(R.id.toggleButtonAccessibility)
        val tButtonOverlay : ToggleButton = findViewById(R.id.toggleButtonOverlay)

        tButtonMain.isEnabled = !App.isSnoozed
        tButtonAccessibility.isChecked = App.isAccessibilityServiceConnected

        if (!App.onMission)
            buttonStopMission.visibility = View.INVISIBLE

        tButtonMain.isChecked = App.isForegroundServiceActive
        tButtonAccessibility.isEnabled = !tButtonMain.isChecked
        tButtonOverlay.isEnabled = !tButtonMain.isChecked
        buttonPermissionSettings.isEnabled = !tButtonMain.isChecked

        tButtonOverlay.isChecked = Settings.canDrawOverlays(this)
    }

//    private val activityResultLauncher = registerForActivityResult(
//        StartActivityForResult()) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(intent)
//    }
}