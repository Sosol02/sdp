package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.geolocation.model.Coordinates;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Monads;
import com.github.onedirection.utils.TimeUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Methods to interact with Google Calendar using our classes.
 */
public final class GoogleCalendar {

    static final String LOGCAT_TAG = "GCalendar";

    // Required string ; should be changed only if one understands the interactions between oAuth2, GoogleSignIn and Google Calendar
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String DEFAULT_NAME = "No Name";

    private static final String CALENDAR_SUMMARY = "1DirectionBackup";

    private static final Pattern LOCATION_REGEX = Pattern.compile("(?<name>.*)\nlat=(?<lat>[+-]?([0-9]*[.])?[0-9]+);lon=(?<lon>[+-]?([0-9]*[.])?[0-9]+)");

    private GoogleCalendar() {
    }

    /**
     * Remove '-' from Id, as they are not allowed by GCalendar: https://developers.google.com/calendar/v3/reference/events/insert
     */
    private static String fromId(Id id) {
        return id.getUuid().replace("-", "");
    }

    private static Id toId(String str) {
        if (str.length() != Id.LENGTH - Id.SEP_COUNT) {
            throw new IllegalArgumentException("Invalid string for id: " + str);
        }

        String result = new StringJoiner("-")
                .add(str.substring(0, 8))
                .add(str.substring(8, 12))
                .add(str.substring(12, 16))
                .add(str.substring(16, 20))
                .add(str.substring(20))
                .toString();

        return new Id(UUID.fromString(result));
    }

    /**
     * Converts an Event into a Google Calendar Event.
     * If the event is recurring, the conversion can only happen if the event is the first to occur in the recurrence,
     * else an exception is thrown.
     * Event class of the Google Calendar API : https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/
     *
     * @param event (Event): the Event to convert into a Google Calendar Event
     * @return (com.google.api.services.calendar.model.Event): a Google Calendar Event
     */
    public static com.google.api.services.calendar.model.Event toGCalendarEvents(Event event) {
        try {
            Objects.requireNonNull(event);

            com.google.api.services.calendar.model.Event gcEvent = new com.google.api.services.calendar.model.Event()
                    .setSummary(event.getName())
                    .setId(fromId(event.getId()));

            String location = event.getLocationName();
            Optional<Coordinates> coords = event.getCoordinates();
            if (coords.isPresent()) {
                Coordinates coordinates = coords.get();
                location += "\nlat=" + coordinates.latitude + ";lon=" + coordinates.longitude;
            }
            gcEvent.setLocation(location);

            DateTime startDateTime = new DateTime(event.getStartTime().format(DateTimeFormatter.ofPattern(RFC3339_FORMAT)));
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(event.getStartTime().getZone().getId());
            gcEvent.setStart(start);

            DateTime endDateTime = new DateTime(event.getEndTime().format(DateTimeFormatter.ofPattern(RFC3339_FORMAT)));
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(event.getEndTime().getZone().getId());
            gcEvent.setEnd(end);

            return gcEvent;
        } catch (RuntimeException e) {
            Log.d(LOGCAT_TAG, e.getMessage());
            throw e;
        }
    }

    /**
     * Converts a Google Calendar Event into an Event
     * Note: The resulting new Event doesn't have a location specified with coordinates, so technically it doesn't have
     * a location. The only information we have on a potential location is the locationName.
     * The id of the new Event is a new (generated) Id, because one cannot be sure that the Google Calendar Event
     * id is in the same format as the local Event id UUID format.
     *
     * @param event (com.google.api.services.calendar.model.Event) : The Google Calendar Event to convert into an Event
     * @return (Event) : a new Event
     * <p>
     * TODO: return multiple events if recurrent
     */
    public static Event fromGCalendarEvents(com.google.api.services.calendar.model.Event event) {
        try {
            Objects.requireNonNull(event);
            if (event.getStart() == null || event.getEnd() == null) {
                throw new IllegalArgumentException("Event should have at least a start time and an end time.");
            }
            Id newId = toId(event.getId());
            String name = (event.getSummary() != null) ? event.getSummary() : DEFAULT_NAME;
            String locationName = (event.getLocation() != null) ? event.getLocation() : "";
            boolean isFavorite = false;

            long epochSecondStartTime = event.getStart().getDateTime().getValue() / 1000;
            ZonedDateTime startTime = TimeUtils.epochToZonedDateTime(epochSecondStartTime);
            long epochSecondEndTime = event.getEnd().getDateTime().getValue() / 1000;
            ZonedDateTime endTime = TimeUtils.epochToZonedDateTime(epochSecondEndTime);

            Matcher locMatch = LOCATION_REGEX.matcher(locationName);
            if (locMatch.matches()) {
                return new Event(
                        newId,
                        name,
                        new NamedCoordinates(
                                Double.parseDouble(Objects.requireNonNull(locMatch.group("lat"))),
                                Double.parseDouble(Objects.requireNonNull(locMatch.group("lon"))),
                                locMatch.group("name")),
                        startTime,
                        endTime,
                        false
                );
            } else {
                return new Event(newId, name, locationName, startTime, endTime, false);
            }
        } catch (RuntimeException e) {
            Log.d(LOGCAT_TAG, e.getMessage());
            throw e;
        }
    }

    private static List<String> listMatchingCalendarIds(Calendar service, String summary) {
        List<String> ls = new ArrayList<>();

        try {
            String pageToken = null;
            do {
                CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals(summary)) {
                        ls.add(calendarListEntry.getId());
                    }
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);
        } catch (Exception ignored) {
            Log.d(LOGCAT_TAG, "Calendars loading failed.");
        }
        return ls;
    }

    private static List<com.google.api.services.calendar.model.Event> listCalendarEvents(Calendar service, String calendar) {
        List<com.google.api.services.calendar.model.Event> allEvents = new ArrayList<>();

        try {
            String pageToken = null;
            do {
                Events events = service.events().list(calendar).setPageToken(pageToken).execute();
                allEvents.addAll(events.getItems());

                pageToken = events.getNextPageToken();
            } while (pageToken != null);
        } catch (Exception e) {
            Log.d(LOGCAT_TAG, "Events loading failed.");
        }

        return allEvents;
    }

    /* Currently unused; left if needed to improve events export */
    @VisibleForTesting
    public static List<Event> removeRecurrent(List<Event> events) {
        List<Event> roots = new ArrayList<>();
        Set<Id> rootIds = new HashSet<>();
        Set<Id> recIds = new HashSet<>();

        for (Event event : events) {
            if (!event.isRecurrent() || event.getId().equals(event.getRecurrence().get().getGroupId())) {
                // Root event
                rootIds.add(event.getId());
                roots.add(event);
            } else {
                recIds.add(event.getRecurrence().get().getGroupId());
            }
        }

        recIds.removeAll(rootIds);
        if (!recIds.isEmpty()) {
            throw new IllegalArgumentException("Some recurrence roots were not found: " + recIds.toString());
        }

        return roots;
    }

    private static Calendar setupCalendar(Context ctx, Account account) throws
            IOException, GoogleAuthException {
        String token = GoogleAuthUtil.getToken(ctx, account, OAUTH_SCOPE);
        GoogleCredential credential = new GoogleCredential().setAccessToken(token);
        return new Calendar.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                .setApplicationName(ctx.getApplicationInfo().name)
                .build();
    }

    public static CompletableFuture<Void> exportEvents(Context ctx, Account
            account, CompletableFuture<List<Event>> ls) {
        return ls
                .thenApply(f -> Monads.map(f, GoogleCalendar::toGCalendarEvents))
                .thenCompose(events ->
                        // This is needed to ensure that this is not run on the main thread
                        // (which is a precondition of getToken)
                        CompletableFuture.runAsync(() -> {
                            int counter = 0;
                            try {
                                Calendar service = setupCalendar(ctx, account);

                                // Remove matching calendars to avoid duplicates
                                List<String> ids = listMatchingCalendarIds(service, CALENDAR_SUMMARY);
                                for (String id : ids) {
                                    Log.d(LOGCAT_TAG, "Deleting calendar: " + id);
                                    service.calendars().delete(id).execute();
                                }


                                Log.d(LOGCAT_TAG, "Creating calendar...");
                                String calendarId = service.calendars().insert(
                                        new com.google.api.services.calendar.model.Calendar()
                                                .setDescription("Calendar generated by the 1Direction app.")
                                                .setSummary(CALENDAR_SUMMARY)
                                ).execute().getId();
                                Log.d(LOGCAT_TAG, "... done (id " + calendarId + ")");

                                for (com.google.api.services.calendar.model.Event event : events) {
                                    service.events().insert(calendarId, event).execute();
                                    counter += 1;
                                }

                                Log.d(LOGCAT_TAG, counter + " events were successfully exported");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(LOGCAT_TAG,
                                        "Events export failed (" +
                                                counter +
                                                "/" +
                                                events.size() +
                                                " events exported): " +
                                                e.getMessage()
                                );
                            }
                        }));
    }


    public static CompletableFuture<Void> importEvent(Context ctx, Account account) {
        CompletableFuture<List<Event>> gcalendarF = CompletableFuture.supplyAsync(() -> {
            try {
                Calendar service = setupCalendar(ctx, account);

                // Remove matching calendars to avoid duplicates
                List<String> ids = listMatchingCalendarIds(service, CALENDAR_SUMMARY);
                if (ids.size() > 1) {
                    Log.w(LOGCAT_TAG, "Multiple matching calendars found.");
                } else if (ids.isEmpty()) {
                    Log.w(LOGCAT_TAG, "No matching calendar found.");
                    return Collections.emptyList();
                }

                List<Event> events = new ArrayList<>();
                for (String id : ids) {
                    events.addAll(
                            Monads.map(
                                    listCalendarEvents(service, id),
                                    GoogleCalendar::fromGCalendarEvents
                            )
                    );
                }

                Log.d(LOGCAT_TAG, events.size() + " events were successfully imported");
                return events;

            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
                Log.d(LOGCAT_TAG,
                        "Events import failed (" +
                                e.getMessage()
                );
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<List<Event>> databaseF = EventQueries.getAllEvents(Database.getDefaultInstance());

        return CompletableFuture.allOf(gcalendarF, databaseF).thenAccept(aVoid -> {
            try {
                List<Event> gcalendar = gcalendarF.get();
                Set<Id> database = new HashSet<>(Monads.map(databaseF.get(), Event::getId));

                List<Event> newEvents = new ArrayList<>();

                Log.d(LOGCAT_TAG, gcalendar.size() + " g-events found.");

                for (Event event : gcalendar) {
                    if (!database.contains(event.getId())) {
                        newEvents.add(event);
                    }
                }

                Log.d(LOGCAT_TAG, newEvents.size() + " g-events to add to DB.");
                EventQueries adder = new EventQueries(Database.getDefaultInstance());

                for (Event event : newEvents) {
                    if (event.isRecurrent()) {
                        adder.addRecurringEvent(event);
                    } else {
                        adder.addNonRecurringEvent(event);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.d(LOGCAT_TAG, "One of the import operations failed.");
                throw new RuntimeException(e);
            }
        });
    }
}

