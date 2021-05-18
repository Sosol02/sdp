package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;

import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.TimeUtils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.Event;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Methods to interact with Google Calendar using our classes.
 */
public final class GoogleCalendar {

    // Don't ask my the hows-and-whys of this string, all I know is that it doesn't work otherwise
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    static final String LOGCAT_TAG = "GCalendar";
    private static final String DEFAULT_NAME = "No Name";

    private GoogleCalendar() {
    }

    public static Event toGCalendarEvents(com.github.onedirection.event.Event event) {
        // TODO: Task #207 (assigned to @Flechman)

        // Example: https://developers.google.com/calendar/v3/reference/events/insert#java
        // Doc: https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/
        // Warning: use EventDateTime::setDatetime, not EventDateTime::setDate

        return null;
    }

    /**
     *
     * Note: The resulting new Event doesn't have a location specified with coordinates, so technically it doesn't have
     * a location. The only information we have on a potential location is the locationName.
     * The id of the new Event is a new (generated) Id, because one cannot be sure that the Google Calendar Event
     * id is in the same format as the local Event id UUID format.
     * @param event
     * @return
     */
    public static com.github.onedirection.event.Event fromGCalendarEvents(Event event) {
        Objects.requireNonNull(event);
        if(event.getStart() == null || event.getEnd() == null) {
            throw new IllegalArgumentException("Event should have at least a start time and an end time.");
        }

        Id newId = Id.generateRandom();
        String name = (event.getSummary() != null) ? event.getSummary() : DEFAULT_NAME;
        //TODO: ask @Ef55 what default location name to put if no location is specified
        String locationName = (event.getLocation() != null) ? event.getLocation() : "";

        long epochSecondStartTime = event.getStart().getDateTime().getValue()/1000;
        ZonedDateTime startTime = TimeUtils.epochToZonedDateTime(epochSecondStartTime);
        long epochSecondEndTime = event.getEnd().getDateTime().getValue()/1000;
        ZonedDateTime endTime = TimeUtils.epochToZonedDateTime(epochSecondEndTime);

        com.github.onedirection.event.Event newEvent =
                new com.github.onedirection.event.Event(newId, name, locationName, startTime, endTime);

        if(event.getRecurrence() != null) {
            List<String> recurrences = event.getRecurrence();
            for(String rule : recurrences) {
                if(rule.substring(0, 5).equals("RRULE")) {
                    String[] info = rule.substring(11).split(";COUNT=");
                
                }
            }
        }

        return newEvent;
    }

    public static CompletableFuture<Void> exportEvents(Context ctx, Account account, CompletableFuture<List<com.github.onedirection.event.Event>> ls) {
        // TODO: Task #206 -- See `gcalendar-export` branch for WIP implementation
        return ls.thenAccept(ignored -> {
        });
    }
}
