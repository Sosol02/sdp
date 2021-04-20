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

import com.github.onedirection.EventQueries;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.Event;
import com.github.onedirection.R;
import com.github.onedirection.utils.Pair;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Notifications {
    public static final String NOTIF_BROADCAST = "notif_broadcast";
    public static final String NOTIF_ID = "notif_id";
    public static final String NOTIFICATION_PAYLOAD = "notif_payload";
    private static final String CHANNEL_ID = "com.1Direction.events";
    private static final String CHANNEL_NAME = "1directionChannel";
    private static final String CHANNEL_DESC = "1Direction event notifications";
    private static final String LOG_TAG = "NOTIFICATIONS";
    private static Notifications global = null;

    private final Map<Integer, Event> eventIdMap = new HashMap<>();
    private final Map<Integer, Notification> notifMap = new HashMap<>();
    // The pending intent allows canceling the alarm manager trigger
    private final Map<Integer, PendingIntent> notifIntentMap = new HashMap<>();
    private Integer currentlyScheduledId = null;

    public static Notifications getInstance(Context context) {
        if (global == null) {
            setupNotifChannel(context);
            global = new Notifications();
        }
        return global;
    }

    private static void setupNotifChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notifications() {}
    
    private int getEventId(Event event) {
        for (Map.Entry<Integer, Event> entry : eventIdMap.entrySet()) {
            if (entry.getValue().equals(event)) {
                return entry.getKey();
            }
        }
        int id = NotificationIdGenerator.getUniqueId();
        eventIdMap.put(id, event);
        return id;
    }

    private Optional<Event> getClosestEvent(List<Event> events, ZonedDateTime now) {
        return events.stream()
                .filter(e -> e.getStartTime().isAfter(now))
                .min((l, r) -> {
                    if (l.equals(r)) {
                        return 0;
                    } else {
                        return l.getStartTime().isBefore(r.getStartTime()) ? -1 : 1;
                    }
                });
    }

    private Notification getNotification(Event event, Context context) {
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

    public void notificationWasSentForId(int id) {
        Log.d(LOG_TAG, "notificationWasSentForId: " + id);
        currentlyScheduledId = null;
        eventIdMap.remove(id);
        notifIntentMap.remove(id);
        notifMap.remove(id);
    }
    
    public void schedule(Context context, long whenMillis, int notifId, Notification notif, BroadcastReceiver handler) {
        long now = System.currentTimeMillis();
        if (whenMillis < now) {
            Log.d("schedule", "Registering notification with due date in the past.");
        }

        currentlyScheduledId = notifId;

        Intent intent = new Intent(context, handler.getClass());
        intent.putExtra(NOTIF_ID, notifId);
        intent.putExtra(NOTIFICATION_PAYLOAD, notif);

        PendingIntent pending = PendingIntent.getBroadcast(context, notifId, intent, 0);

        // remember the notif we scheduled
        notifMap.put(notifId, notif);
        notifIntentMap.put(notifId, pending);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
    }

    public CompletableFuture<Integer> scheduleClosestEvent(Context context) {
        /*
        // Find earliest event
        Optional<Event> optEv = Optional.empty();
        for (Event e : events) {
            if (!optEv.isPresent())
                optEv = Optional.of(e);
            if (e.getStartTime().isBefore(optEv.get().getStartTime())) {
                optEv = Optional.of(e);
            }
        }// */
        //Optional<Event> optEv = events.stream().min((l, r) -> l.getStartTime().toInstant().isBefore(r.getStartTime().toInstant()) ? 1 : -1);
        /*
        Log.d("scheduleClosestEvent", "optEv: " + optEv);
        Log.d("scheduleClosestEvent", "events: " + events);
        if (optEv.isPresent()) {
            Event closest = optEv.get();
            Log.d("i am dying: ", closest.getStartTime().toInstant().toString());
            int id = scheduleNotif(context, closest.getStartTime().toEpochSecond() * 1000,
                    closest.getName(),
                    closest.getStartTime().getHour() + ":" +
                            closest.getStartTime().getMinute() + " @ " +
                            closest.getLocationName());

            currentlyScheduled = Optional.of(closest);
            events.remove(closest);

            return Optional.of(id);
        }
        return Optional.empty(); */

        ZonedDateTime now = ZonedDateTime.now();
        Log.d(LOG_TAG, "scheduleClosestEvent: " + now);
        CompletableFuture<List<Event>> events = EventQueries.getEventsByDay(Database.getDefaultInstance(), now);

        CompletableFuture<Integer> future = new CompletableFuture<>();

        events.whenComplete((ls, err) -> {
            Log.d(LOG_TAG, "whenComplete: err:" + err + " ls: " + ls);
            if (err != null) {
                Log.d(LOG_TAG, "DB query had an exception: " + err);
                future.completeExceptionally(err);
            }

            Optional<Event> closestOpt = getClosestEvent(ls, now);
            if (!closestOpt.isPresent()) {
                Log.d(LOG_TAG, "No closest event. " + ls);
                return; // Don't schedule anything
            }
            Event closest = closestOpt.get();
            int id = getEventId(closest); // register to the eventIdMap

            if (currentlyScheduledId == null) {
                currentlyScheduledId = id;
                Log.d(LOG_TAG, "currentlyScheduledId was null, now id: " + currentlyScheduledId);
                long whenMillis = closest.getStartTime().toInstant().toEpochMilli();
                schedule(context, whenMillis, id, getNotification(closest, context), new NotificationPublisher() /* maybe should not recreate one ever time?*/);
                future.complete(id);
                return;
            }

            if (currentlyScheduledId != id) {
                Event currentlyScheduledEvent = eventIdMap.get(currentlyScheduledId);
                if (currentlyScheduledEvent.getStartTime().isBefore(closest.getStartTime())) {
                    Log.d(LOG_TAG, "Currently scheduled event is before earliest event in DB.");
                    future.complete(currentlyScheduledId);
                } else {
                    // the already scheduled event is later, cancel it and schedule this one
                    PendingIntent currentPendingIntent = notifIntentMap.get(currentlyScheduledId);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(currentPendingIntent);

                    long whenMillis = closest.getStartTime().toInstant().toEpochMilli();
                    Log.d(LOG_TAG, "Canceling old event and scheduling new one.");
                    schedule(context, whenMillis, id, getNotification(closest, context), new NotificationPublisher() /* maybe should not recreate one ever time?*/);
                }
            } else {
                // Already scheduled, nothing to do
                Log.d(LOG_TAG, "Current earliest event is still already scheduled");
                future.complete(id);
            }
        });

        return future;
    }

    public int scheduleTextNotif(Context context, long whenMillis, String title, String text) {
        Notification notif = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_home) // TODO: make actual icon
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // ignored on android 23+
                .setAutoCancel(true)
                .build();

        int id = NotificationIdGenerator.getUniqueId();
        schedule(context, whenMillis, id, notif, new NotificationPublisher());

        return id;
    }

/*
    public void scheduleEventNotifs(Context context, Set<Event> eventList) {
        events.addAll(eventList);
        scheduleClosestEvent(context);
    }*/
/*
    public void testNotif(Context context) {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Event> evs = new HashSet<>();
        evs.add(new Event(Id.generateRandom(), "First event", "EPFL", Optional.empty(), now.plusSeconds(50), now.plusSeconds(1000)));
        evs.add(new Event(Id.generateRandom(), "Second event", "Route 5", Optional.empty(), now.plusSeconds(80), now.plusSeconds(1200)));
        evs.add(new Event(Id.generateRandom(), "Third event", "Heaven", Optional.empty(), now.plusSeconds(100), now.plusSeconds(1500)));

        scheduleEventNotifs(context, evs);

        //scheduleNotif(context, System.currentTimeMillis() + 5000, "Test notif", "u.u");
    }*/
}
