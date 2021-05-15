package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.onedirection.utils.Monads;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public final class GoogleCalendar {

    // Don't ask my the hows-and-whys of this string, all I know is that it doesn't work otherwise
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    static final String LOGCAT_TAG = "GCalendar";

    private GoogleCalendar() {
    }

    public static Event toGCalendarEvents(com.github.onedirection.event.Event event){
        // Task #207 (assigned to @Flechman)
        return null;
    }

    public static com.github.onedirection.event.Event fromGCalendarEvents(Event event){
        return null;
    }

    // This method is a pure POC (that we can access GCalendar).
    // As such, it currently doesn't do what it should,
    // is untested and more generally is terrible code.
    public static CompletableFuture<Void> exportEvents(Context ctx, Account account, CompletableFuture<List<com.github.onedirection.event.Event>> ls) {
        // Task #206 (assigned to @Ef55)
        return ls.thenApply(f -> Monads.map(f, GoogleCalendar::toGCalendarEvents)).thenAccept(events -> {
            try {
                String token = GoogleAuthUtil.getToken(ctx, account, OAUTH_SCOPE);
                Log.d(LOGCAT_TAG, "Exporting events...");

                GoogleCredential credential = new GoogleCredential().setAccessToken(token);
                Calendar calendar = new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                        .setApplicationName("Auth Code Exchange Demo")
                        .build();

                DateTime now = new DateTime(System.currentTimeMillis());
                Events events1 = calendar.events().list("primary")
                        .setMaxResults(10)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events1.getItems();
                if (items.isEmpty()) {
                    Log.d(LOGCAT_TAG, "No upcoming events found.");
                } else {
                    Log.d(LOGCAT_TAG, "Upcoming events");
                    for (Event event : items) {
                        DateTime start = event.getStart().getDateTime();
                        if (start == null) {
                            start = event.getStart().getDate();
                        }
                        Log.d(LOGCAT_TAG, event.getSummary() + start);
                    }
                }
            } catch (GoogleAuthException | IOException e) {
                Log.w(LOGCAT_TAG, "Export failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
