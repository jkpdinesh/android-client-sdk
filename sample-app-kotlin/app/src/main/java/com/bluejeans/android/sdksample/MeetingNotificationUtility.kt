/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object MeetingNotificationUtility {
    const val MEETING_NOTIFICATION_ID = 1
    private const val CHANNEL_ID = "meeting_notification"
    private const val CHANNEL_NAME = "Meeting Notification"
    private var mNotification: NotificationCompat.Builder? = null

    fun updateNotificationMessage(context: Context, msg: String?) {
        if (mNotification != null) {
            mNotification!!.setContentText(msg)
            getNotificationManager(context).notify(MEETING_NOTIFICATION_ID, mNotification!!.build())
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            getNotificationManager(context).createNotificationChannel(serviceChannel)
        }
    }

    fun getNotification(context: Context): NotificationCompat.Builder? {
        mNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.resources.getString(R.string.app_name))
            .setContentText(context.resources.getString(R.string.meeting_notification_message))
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.mipmap.ic_launcher)
        return mNotification
    }

    fun clearMeetingNotification(context: Context) {
        getNotificationManager(context).cancel(MEETING_NOTIFICATION_ID)
        mNotification = null
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(NotificationManager::class.java)
    }
}