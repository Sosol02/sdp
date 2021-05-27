package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.model.Recurrence;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Methods to interact with Google Calendar using our classes.
 */
public final class GoogleCalendar {

    // Don't ask my the hows-and-whys of this string, all I know is that it doesn't work otherwise
    private static final String OAUTH_SCOPE = "oauth2:profile email";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    static final String LOGCAT_TAG = "GCalendar";
    private static final String DEFAULT_NAME = "No Name";

    private final static Map<String, TemporalUnit> PERIODS = Collections.unmodifiableMap(new HashMap<String, TemporalUnit>() {
        {
            put("DAILY", ChronoUnit.DAYS);
            put("WEEKLY", ChronoUnit.WEEKS);
            put("MONTHLY", ChronoUnit.MONTHS);
            put("YEARLY", ChronoUnit.YEARS);
        }
    });

    private static final String CALENDAR_SUMMARY = "1DirectionBackup";

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
        // Put back the '-'; not in the cleanest way tho
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

            if (event.isRecurrent()) {
                Recurrence recurrence = event.getRecurrence().get();
                if (!event.getId().equals(recurrence.getGroupId())) {
                    throw new IllegalArgumentException("Cannot convert a recurring event to a google event if this one is not the first element of the recurrence");
                }
                gcEvent.setRecurringEventId(recurrence.getGroupId().getUuid());

                String recurrencePeriod = null;
                for (Map.Entry<String, TemporalUnit> t : PERIODS.entrySet()) {
                    if (recurrence.getPeriod().equals(t.getValue().getDuration())) {
                        recurrencePeriod = t.getKey();
                    }
                }
                if (recurrencePeriod == null) {
                    throw new IllegalArgumentException("The event recurrence period does not match any possible periods proposed.");
                }

                long count = (recurrence.getEndTime().toEpochSecond() - event.getStartTime().toEpochSecond()) / recurrence.getPeriod().getSeconds() + 1;

                String[] recurr = new String[]{"RRULE:FREQ=" + recurrencePeriod + ";COUNT=" + count};
                gcEvent.setRecurrence(Arrays.asList(recurr));
            }

            // TODO: feature - geolocate event back
            gcEvent.setLocation(event.getLocationName());

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

            Event newEvent = new Event(newId, name, locationName, startTime, endTime, isFavorite);

            if (event.getRecurrence() != null) {
                List<String> recurrences = event.getRecurrence();
                String periodically = null;
                int eventCount = 0;
                boolean rule_found = false;
                String ruleFoundString = "";

                for (String rule : recurrences) {
                    //Refer to https://developers.google.com/calendar/create-events to see how to retrieve recurrence
                    if (rule.startsWith("RRULE")) {
                        if (rule_found && !rule.equals(ruleFoundString)) {
                            throw new IllegalArgumentException("There are two different matching rule formats for the recurrence.");
                        }
                    }
                    if (periodically == null) {
                        throw new IllegalArgumentException("No recurrence rule matches the one used by " + R.string.app_name + " Events");
                    }

                    TemporalUnit period = PERIODS.getOrDefault(periodically, null);
                    if (period == null) {
                        throw new IllegalArgumentException("The event recurrence period does not match any possible periods proposed.");
                    }

                    ZonedDateTime recEndTime = TimeUtils.epochToZonedDateTime
                            (startTime.toEpochSecond() + (eventCount - 1) * period.getDuration().getSeconds());

                    Recurrence newRecurrence = new Recurrence(newEvent.getId(), period.getDuration(), recEndTime);
                    newEvent = newEvent.setRecurrence(newRecurrence);
                }
            }

                return newEvent;
            } catch(RuntimeException e){
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
                .thenApply(GoogleCalendar::removeRecurrent)
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

