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

import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;

import com.github.onedirection.database.Database;
import com.github.onedirection.database.DefaultDatabase;
import com.github.onedirection.database.ObservableDatabase;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.Event;
import com.github.onedirection.R;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Notifications {
    private static final String CHANNEL_ID = "com.1Direction.events";
    private static final String CHANNEL_NAME = "1directionChannel";
    private static final String CHANNEL_DESC = "1Direction event notifications";
    private static final String LOG_TAG = "NOTIFICATIONS";
    private static Notifications global = null;
    private static final int requestCode = 0;
    /**
     * Delta time in seconds within which we consider an event to trigger.
     * Consider looking at onRecieve to get the full picture.
     */
    public static int SLACK = 60;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static int CHECK_WAIT_TIME_SECS = 60 * 5;

    private PendingIntent lastPendingIntent = null;

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
        // Only need to register NotificationChannel for android O and later.
        // Other versions don't need to register them.
        // This is taken straight from Android docs
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        scheduleNextCheckAt(context, ZonedDateTime.now().plusSeconds(3), new NotificationPublisher());
        installNotificationsObserver(context);
    }

    @VisibleForTesting
    public void installNotificationsObserver(Context context) {
        DefaultDatabase.getDefaultInstance().addObserver((observer, obj) -> {
            if (obj.kind == ObservableDatabase.ActionKind.Store || obj.kind == ObservableDatabase.ActionKind.Remove) {
                lastPendingIntent.cancel();
                onCheck(context, new NotificationPublisher());
            }
        });
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

    void onCheck(Context context, BroadcastReceiver handler) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ZonedDateTime now = ZonedDateTime.now();
        CompletableFuture<List<Event>> eventsFuture = EventQueries.getEventsByDay(Database.getDefaultInstance(), now);
        eventsFuture.whenComplete((events, err) -> {
            Context appContext = context.getApplicationContext();

            Log.d(LOG_TAG, "err: " + err + ", events: " + events);
            if (err != null) {
                Log.d(LOG_TAG, "ERROR ENCOUNTERED: " + err);
            } else {
                Objects.requireNonNull(events);
                events.sort((l, r) -> {
                    if (l.equals(r)) return 0;
                    return l.getStartTime().isBefore(r.getStartTime()) ? -1 : 1;
                });
                Log.d(LOG_TAG, "Sorted events: " + events);
                // Events is now sorted by Events's start date (early to late):
                // We look through and: if an event happens before now ~ slack,
                // we ignore it ; if it happens within now +- slack, we notify ;
                // the first to happen after now + slack is the time we want this
                // function to be called again.
                ZonedDateTime lowerBound = now.minusSeconds(SLACK);
                ZonedDateTime upperBound = now.plusSeconds(SLACK);
                boolean scheduledNextTime = false;
                for (Event e : events) {
                    if (e.getStartTime().isBefore(lowerBound)) {
                        Log.d(LOG_TAG, "Event happens before now: " + e);
                        continue;
                    }
                    if (e.getStartTime().isAfter(upperBound)) {
                        Log.d(LOG_TAG, "First event that happens after now+slack: " + e);
                        scheduleNextCheckAt(appContext, e.getStartTime(), handler);
                        scheduledNextTime = true;
                        break;
                    }
                    // e is within lowerBound..upperBound
                    Notification notif = getNotification(e, appContext);
                    int notifId = NotificationIdGenerator.getUniqueId();
                    Log.d(LOG_TAG, "Send notifs: notif: " + notif + ", id: " + notifId);
                    nm.notify(notifId, notif);
                }
                if (!scheduledNextTime) {
                    Log.d(LOG_TAG, "No event was available for scheduling the next call, " +
                            "schdeling for next check = " + CHECK_WAIT_TIME_SECS + " seconds.");
                    scheduleNextCheckAt(appContext, now.plusSeconds(CHECK_WAIT_TIME_SECS), handler);
                }
            }
        });
    }

    void scheduleNextCheckAt(Context context, ZonedDateTime when, BroadcastReceiver handler) {
        ZonedDateTime now = ZonedDateTime.now();
        if (when.isBefore(now)) {
            Log.d("schedule", "Registering handler with due date in the past: now: " + now + ", when: " + when);
        }

        Intent intent = new Intent(context, handler.getClass());
        // https://stackoverflow.com/questions/21526319/whats-requestcode-used-for-on-pendingintent
        // The request code seems to not really matter for our usecase.
        if (lastPendingIntent != null) {
            lastPendingIntent.cancel();
        }
        final PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        lastPendingIntent = pending;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when.toInstant().toEpochMilli(), pending);
    }
}
