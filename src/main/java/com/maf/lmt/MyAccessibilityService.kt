package com.maf.lmt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "zBug" + javaClass.simpleName
    private var activeServiceInfo = AccessibilityServiceInfo()
    private val inactiveServiceInfo = AccessibilityServiceInfo()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "onReceive: broadcast received")
            when(intent?.action) {
                Constants.ACTION_START_TRACKING -> serviceInfo = activeServiceInfo
                Constants.ACTION_STOP_TRACKING -> serviceInfo = inactiveServiceInfo
            }
        }
    }
//    private var currentWindow = String()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: called")

        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_START_TRACKING)
        filter.addAction(Constants.ACTION_STOP_TRACKING)
        registerReceiver(receiver,filter)

//        active.packageNames = arrayOf("com.android.documentsui")
        activeServiceInfo.packageNames = arrayOf("com.facebook.lite",
            "com.facebook.katana",
            "com.pinterest",
            "com.instagram.android")

        inactiveServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

        activeServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED
        activeServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
//        active.notificationTimeout = 100
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected: accessibility")
        App.isAccessibilityServiceConnected = true

        if(!App.isForegroundServiceActive) serviceInfo = inactiveServiceInfo
        else serviceInfo = activeServiceInfo

        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        if(event!!.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            if(event.packageName == "com.android.launcher3" || event.packageName == "com.miui.home") return
////            if(event.packageName == "com.android.systemui") currentWindow = ""
//        }
//        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
////            if(event.packageName == "com.android.systemui" || event.packageName == currentWindow) return
//            if(event.packageName == currentWindow) return
//            currentWindow = event.packageName.toString()
//        }

//        if(event!!.packageName == "com.facebook.lite") {
            sendBroadcast(Intent("TRIGGERED").putExtra("packageName",event!!.packageName.toString()))
//        }

//        if(currentWindow != event.packageName.toString()) {
//            currentWindow = event.packageName.toString()
//
//        }
//        if(event.packageName == "com.android.documentsui")
//            sendBroadcast(Intent("TRIGGERED").putExtra("packageName",event.packageName.toString()))

        Log.i(TAG, "onAccessibilityEvent: " + event.toString().substring(0,40) + " " + event.packageName)

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind: accessibility service disconnected")
        App.isAccessibilityServiceConnected = false
        unregisterReceiver(receiver)
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}