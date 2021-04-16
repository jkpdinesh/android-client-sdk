/*
 * Copyright (c) 2021 Blue Jeans Network, Inc. All rights reserved.
 */
package com.bluejeans.android.sdksample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * It is recommended to start a foreground service before getting into the meeting.
 * Starting a foreground service ensures we have all the system resources available to our app
 * even when in background, thereby not compromising on audio quality, content capture quality
 * during features like content share and also prevents app from being killed due to lack of resources.
 */
public class OnGoingMeetingService extends Service {

    private static final String TAG = "OnGoingMeetingService";

    public static void startService(Context context) {
        Log.i(TAG, "Starting service");
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, OnGoingMeetingService.class);
        context.startForegroundService(serviceIntent);
    }

    public static void stopService(Context context) {
        Log.i(TAG, "Stopping service");
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, OnGoingMeetingService.class);
        context.stopService(serviceIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MeetingNotificationUtility.createNotificationChannel(this);
        NotificationCompat.Builder notification = MeetingNotificationUtility.getNotification(this);
        startForeground(MeetingNotificationUtility.MEETING_NOTIFICATION_ID, notification.build());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service Destroyed");
        stopForeground(true);
        MeetingNotificationUtility.clearMeetingNotification(this);
        super.onDestroy();
    }
}
