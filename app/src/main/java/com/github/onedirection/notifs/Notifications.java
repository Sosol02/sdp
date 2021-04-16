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

import com.github.onedirection.events.Event;
import com.github.onedirection.R;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Notifications {
    public static final String NOTIF_BROADCAST = "notif_broadcast";
    public static final String NOTIF_ID = "notif_id";
    public static final String NOTIFICATION_PAYLOAD = "notif_payload";

    private static Notifications global = null;
    public static Notifications getInstance(Context context) {
        if (global == null) {
            setupNotifChannel(context);
            global = new Notifications();
        }
        return global;
    }

    private static final String CHANNEL_ID = "com.1Direction.events";
    private static final String CHANNEL_NAME = "1directionChannel";
    private static final String CHANNEL_DESC = "1Direction event notifications";

    private static void setupNotifChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final Set<Event> events = new HashSet<>();
    private Optional<Event> currentlyScheduled = Optional.empty();

    public void schedule(Context context, long whenMillis, int notifId, /* TODO fix that */ Parcelable payload, BroadcastReceiver handler) {
        long now = System.currentTimeMillis();
        if (whenMillis < now) {
            Log.d("schedule", "Registering notification with due date in the past.");
        }

        Intent intent = new Intent(context, handler.getClass());
        intent.putExtra(NOTIF_ID, notifId);
        intent.putExtra(NOTIFICATION_PAYLOAD, payload);

        PendingIntent pending = PendingIntent.getBroadcast(context, notifId, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pending);
    }

    public int scheduleNotif(Context context, long whenMillis, String title, String text) {
        Notification notif = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_home) // TODO: make actual icon
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // ignored on android 23+
                .setAutoCancel(true)
                .build();

        int id = NotificationIdGenerator.getUniqueId();
        schedule(context, whenMillis, id, notif, new NotificationPublisher(context));
        
        return id;
    }

    public Optional<Integer> scheduleClosestEvent(Context context) {
        // /*
        Optional<Event> optEv = Optional.empty();
        for (Event e : events) {
            if (!optEv.isPresent())
                optEv = Optional.of(e);
            if (e.getStartTime().isBefore(optEv.get().getStartTime())) {
                optEv = Optional.of(e);
            }
        }// */
        //Optional<Event> optEv = events.stream().min((l, r) -> l.getStartTime().toInstant().isBefore(r.getStartTime().toInstant()) ? 1 : -1);
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
        return Optional.empty();
    }

    public void scheduleEventNotifs(Context context, Set<Event> eventList) {
        events.addAll(eventList);
        scheduleClosestEvent(context);
    }
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
