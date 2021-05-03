package com.github.onedirection.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.github.onedirection.EventQueries;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.Event;
import com.github.onedirection.R;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Notifications {
    private static final String CHANNEL_ID = "com.1Direction.events";
    private static final String CHANNEL_NAME = "1directionChannel";
    private static final String CHANNEL_DESC = "1Direction event notifications";
    private static final String LOG_TAG = "NOTIFICATIONS";
    private static Notifications global = null;

    public static Notifications getInstance(Context context) {
        if (global == null) {
            global = new Notifications(context);
        }
        return global;
    }

    private Notifications(Context context) {
        setupNotifChannel(context);
    }

    private void setupNotifChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        schedule(context, ZonedDateTime.now().plusSeconds(3).toInstant().toEpochMilli(), new NotificationPublisher());
    }
    
    Notification getNotification(Event event, Context context) {
        String text =
                event.getStartTime().getHour() + ":" +
                event.getStartTime().getMinute() + " @ " +
                event.getLocationName();

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_home) // TODO: make actual icon
                .setContentTitle(event.getName())
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // ignored on android 23+
                .setAutoCancel(true)
                .build();
    }
    
    void schedule(Context context, long whenMillis, BroadcastReceiver handler) {
        long now = System.currentTimeMillis();
        if (whenMillis < now) {
            Log.d("schedule", "Registering handler with due date in the past: now: " + now + ", whenMillis: " + whenMillis);
        }

        Intent intent = new Intent(context, handler.getClass());
        // TODO: check what requestCode does. used to be notifId.
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
    }
}
