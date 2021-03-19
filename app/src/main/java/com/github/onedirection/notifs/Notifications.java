package com.github.onedirection.notifs;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;
import java.util.function.Consumer;

public class Notifications {
    public static final String NOTIF_BROADCAST = "notif_broadcast";
    public static final String NOTIF_ID = "notif_id";
    public static final String PAYLOAD = "notif_payload";

    private static Notifications global = null;
    public static Notifications getInstance(Context context) {
        if (global == null) {
            setupNotifChannel(context);
            global = new Notifications();
        }
        return global;
    }

    private static final String CHANNEL_ID = "todo";

    private static void setupNotifChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "1directionChannel";
            String description = "All 1direction messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void testNotif(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "testChannel")
                .setContentTitle("Test Notification")
                .setContentText("Luke, je suis ton père")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.notify(123, builder.build());
    }

    public <T> void schedule(Context context, long whenMillis, int notifId, /* TODO fix that */ String payload, BroadcastReceiver handler) {
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("Text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);*/

        Log.d("NOTIF", "schedule " + whenMillis + " " + new Date(whenMillis).toString() + " " + handler.toString());

        Intent intent = new Intent(context, handler.getClass());
        intent.putExtra(NOTIF_ID, notifId);
        intent.putExtra(PAYLOAD, payload);
        PendingIntent pending = PendingIntent.getActivity(context, notifId, intent, 0);

        context.registerReceiver(handler, new IntentFilter());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, whenMillis, pending);
    }

    public void scheduleNotif(Context context, Date date, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        Notification notif = builder.build();

        Log.d("NOTIF", "scheduleNotif");

        schedule(context, date.getTime(), 42, "i am a payload", new NotificationPublisher());
                /*new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("OMG", "HELP ME IM SLOWLY DYING");
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notif = intent.getParcelableExtra(PAYLOAD);
                int id = intent.getIntExtra(NOTIF_ID, 0);
                nm.notify(id, notif);
            }
        }.getClass());*/
    }
}
