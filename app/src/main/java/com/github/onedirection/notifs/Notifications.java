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
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.onedirection.R;

import java.time.Instant;
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_home)
                .setContentTitle("Test Notification")
                .setContentText("-w-")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        Log.d("testNotif", "Send notification");
        nm.notify(NotificationIdGenerator.getUniqueId(), builder.build());
        scheduleNotif(context, Date.from(Instant.now().plusMillis(5000)), "Scheduled in 5", "owo");
        Log.d("testNotif", "Scheduled notification");
    }

    public void schedule(Context context, long whenMillis, int notifId, /* TODO fix that */ String payload, BroadcastReceiver handler) {
        Log.d("schedule", "schedule " + whenMillis + " " + new Date(whenMillis).toString() + " " + handler.toString());
        Log.d("schedule", "");

        context.registerReceiver(handler, new IntentFilter());

        Intent intent = new Intent(context, handler.getClass());
        intent.putExtra(NOTIF_ID, notifId);
        intent.putExtra(PAYLOAD, payload);
        PendingIntent pending = PendingIntent.getBroadcast(context, notifId, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
    }

    public void scheduleNotif(Context context, Date date, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Notification notif = builder.build();
        Log.d("scheduleNotif", "scheduling");
        int id = NotificationIdGenerator.getUniqueId();
        schedule(context, date.getTime(), id, "i am a payload", new NotificationPublisher());
    }
}
