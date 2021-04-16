/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */

package com.bluejeans.android.sdksample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class MeetingNotificationUtility {

    public static final int MEETING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "meeting_notification";
    private static final String CHANNEL_NAME = "Meeting Notification";
    private static NotificationCompat.Builder mNotification;

    public static void updateNotificationMessage(Context context, String msg) {
        if (mNotification != null) {
            mNotification.setContentText(msg);
            getNotificationManager(context).notify(MEETING_NOTIFICATION_ID, mNotification.build());
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            getNotificationManager(context).createNotificationChannel(serviceChannel);
        }
    }

    public static NotificationCompat.Builder getNotification(Context context) {
        mNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getResources().getString(R.string.meeting_notification_message))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher);
        return mNotification;
    }

    public static void clearMeetingNotification(Context context) {
        getNotificationManager(context).cancel(MEETING_NOTIFICATION_ID);
        mNotification = null;
    }

    private static NotificationManager getNotificationManager(Context context) {
        return context.getSystemService(NotificationManager.class);
    }
}
