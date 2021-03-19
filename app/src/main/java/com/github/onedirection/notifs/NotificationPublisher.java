package com.github.onedirection.notifs;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("OM324111156387G", "HELP ME IM SLOWLY DYING");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = intent.getParcelableExtra(Notifications.PAYLOAD);
        int id = intent.getIntExtra(Notifications.NOTIF_ID, 0);
        nm.notify(id, notif);
    }
}
