package com.github.onedirection.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class is called when it's time to publish an Android notification.
 * It displays the notification, then calls Notifications::scheduleClosestEvent.
 * This will, if another later event is registered, schedule an AlarmManager intent
 * that will recall this function, etc.
 */
public class NotificationPublisher extends BroadcastReceiver {

    private static final String LOG_TAG = "NotificationPublisher";

    public NotificationPublisher() {
        //Empty constructor required
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        Notifications.getInstance(context).onCheck(context, this);
    }
}
