package com.github.onedirection.notifs;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class NotificationPublisher extends BroadcastReceiver {

    private static final String LOG_TAG = "NotificationPublisher";

    public NotificationPublisher(Context context) {
        // Register to allow it to be called by the broadcast intent of the broadcast manager
        context.registerReceiver(this, new IntentFilter());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notif = intent.getParcelableExtra(Notifications.NOTIFICATION_PAYLOAD);
        int id = intent.getIntExtra(Notifications.NOTIF_ID, -1);

        Log.d(LOG_TAG, "-> notif id = " + id);
        Log.d(LOG_TAG, "-> notif = " + notif);

        // Send notification
        nm.notify(id, notif);

        // Required to be able to register to recieve intents
        Context appContext = context.getApplicationContext();

        // Schedule calling this function again when the next event shows up
        Notifications.getInstance(appContext).scheduleClosestEvent(appContext);
    }
}
