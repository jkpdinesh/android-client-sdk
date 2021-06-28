/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * It is recommended to start a foreground service before getting into the meeting.
 * Starting a foreground service ensures we have all the system resources available to our app
 * even when in background, thereby not compromising on audio quality, content capture quality
 * during features like content share and also prevents app from being killed due to lack of resources.
 */
class OnGoingMeetingService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onCreate")
        MeetingNotificationUtility.createNotificationChannel(this)
        val notification = MeetingNotificationUtility.getNotification(this)
        startForeground(MeetingNotificationUtility.MEETING_NOTIFICATION_ID, notification?.build())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.i(TAG, "Service Destroyed")
        stopForeground(true)
        MeetingNotificationUtility.clearMeetingNotification(this)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OnGoingMeetingService"
        fun startService(context: Context) {
            Log.i(TAG, "Starting service")
            val serviceIntent = Intent().apply {
                setClass(context,OnGoingMeetingService::class.java)
            }
            context.startForegroundService(serviceIntent)
        }

        fun stopService(context: Context) {
            Log.i(TAG, "Stopping service")
            val serviceIntent = Intent().apply {
                setClass(context,OnGoingMeetingService::class.java)
            }
            context.stopService(serviceIntent)
        }
    }
}