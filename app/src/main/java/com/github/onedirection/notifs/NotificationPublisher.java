package com.github.onedirection.notifs;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationPublisher", "onReceive");
        Toast.makeText(context, "I am a toast owo", Toast.LENGTH_LONG).show();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = intent.getParcelableExtra(Notifications.PAYLOAD);
        int id = intent.getIntExtra(Notifications.NOTIF_ID, -1);
        Log.d("NotificationPublisher", "-> notif id = " + id);
        Log.d("NotificationPublisher", "-> notif = " + notif);
        nm.notify(id, notif);
    }
}
