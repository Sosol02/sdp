package com.github.onedirection.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;

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
    /**
     * Delta time in seconds within which we consider an event to trigger.
     * Consider looking at onRecieve to get the full picture.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static int SLACK = 30;

    // need public 0 argument constructor to exist otherwise it crashes
    public NotificationPublisher() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        // Register to allow it to be called by the broadcast intent of the broadcast manager
        //context.registerReceiver(this, new IntentFilter());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ZonedDateTime now = ZonedDateTime.now();
        // TODO: use Database.getInstance instead
        CompletableFuture<List<Event>> eventsFuture = EventQueries.getEventsByDay(ConcreteDatabase.getDatabase(), now);
        eventsFuture.whenComplete((events, err) -> {
            Context appContext = context.getApplicationContext();
            Notifications notifications = Notifications.getInstance(appContext);

            Log.d(LOG_TAG, "err: " + err + ", events: " + events);
            if (err != null) {
                Log.d(LOG_TAG, "ERROR ENCOUNTERED: " + err);
            } else {
                Objects.requireNonNull(events);
                events.sort((l, r) ->{
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
                        notifications.schedule(appContext, e.getStartTime(), this);
                        scheduledNextTime = true;
                        break;
                    }
                    // e is within lowerBound..upperBound
                    Notification notif = notifications.getNotification(e, appContext);
                    int notifId = NotificationIdGenerator.getUniqueId();
                    Log.d(LOG_TAG, "Send notifs: notif: " + notif + ", id: " + notifId);
                    nm.notify(notifId, notif);
                }
                if (!scheduledNextTime) {
                    Log.d(LOG_TAG, "No event was available for scheduling the next call, " +
                            "schdeling for slack = " + SLACK + "seconds * 2.");
                    notifications.schedule(appContext, now.plusSeconds(SLACK * 2), this);
                }
            }
        });
    }
}
