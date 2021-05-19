package com.github.onedirection.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.github.onedirection.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.Event;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class is called when it's time to publish a notification.
 * It displays the notification, then calls Notifications::scheduleClosestEvent.
 * This will, if another later event is registered, schedule an AlarmManager intent
 * that will recall this function, etc.
 */
public class NotificationPublisher extends BroadcastReceiver {

    private static final String LOG_TAG = "NotificationPublisher";

    // need public 0 argument constructor to exist otherwise it crashes
    public NotificationPublisher() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        Notifications.getInstance(context).onCheck(context, this);
    }
}
