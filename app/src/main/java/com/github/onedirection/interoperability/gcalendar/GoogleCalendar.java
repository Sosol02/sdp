package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.Event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Methods to interact with Google Calendar using our classes.
 */
public final class GoogleCalendar {

    // Don't ask my the hows-and-whys of this string, all I know is that it doesn't work otherwise
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    static final String LOGCAT_TAG = "GCalendar";

    private GoogleCalendar() {
    }

    public static Event toGCalendarEvents(com.github.onedirection.event.Event event) {
        // TODO: Task #207 (assigned to @Flechman)

        // Example: https://developers.google.com/calendar/v3/reference/events/insert#java
        // Doc: https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/
        // Warning: use EventDateTime::setDatetime, not EventDateTime::setDate

        return null;
    }

    public static com.github.onedirection.event.Event fromGCalendarEvents(Event event) {
        // TODO: Task #???

        return null;
    }

    public static CompletableFuture<Void> exportEvents(Context ctx, Account account, CompletableFuture<List<com.github.onedirection.event.Event>> ls) {
        // TODO: Task #206 -- See `gcalendar-export` branch for WIP implementation
        return ls.thenAccept(ignored -> {
        });
    }
}
