package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.github.onedirection.utils.Monads;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Methods to interact with Google Calendar using our classes.
 */
public final class GoogleCalendar {

    // Don't ask my the hows-and-whys of this string, all I know is that it doesn't work otherwise
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    static final String LOGCAT_TAG = "GCalendar";

    private GoogleCalendar() {
    }

    public static Event toGCalendarEvents(com.github.onedirection.event.Event event) {
        // TODO: Task #207 (assigned to @Flechman)

        // Example: https://developers.google.com/calendar/v3/reference/events/insert#java
        // Doc: https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/
        // Warning: use EventDateTime::setDatetime, not EventDateTime::setDate

        Objects.requireNonNull(event);
        Event gcEvent = new Event()
                .setSummary(event.getName());
        if(event.getLocation().isPresent()) {
            gcEvent.setLocation();
        }

        DateTime startDateTime = new DateTime(event.getStartTime().format(DateTimeFormatter.ofPattern(RFC3339_FORMAT)));
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(ZoneId.SHORT_IDS.get(event.getStartTime().getZone().getId()));
        gcEvent.setStart(start);

        DateTime endDateTime = new DateTime(event.getEndTime().format(DateTimeFormatter.ofPattern(RFC3339_FORMAT)));
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(ZoneId.SHORT_IDS.get(event.getEndTime().getZone().getId()));
        gcEvent.setEnd(end);

        if(event.isRecurrent()) {

        }


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
