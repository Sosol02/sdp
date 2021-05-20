package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Context;

import com.github.onedirection.R;
import com.github.onedirection.event.Event;
import com.github.onedirection.event.Recurrence;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.TimeUtils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String DEFAULT_NAME = "No Name";

    private final static Map<String, TemporalUnit> PERIODS = Collections.unmodifiableMap(new HashMap<String, TemporalUnit>() {
        {
            put("DAILY", ChronoUnit.DAYS);
            put("WEEKLY", ChronoUnit.WEEKS);
            put("MONTHLY", ChronoUnit.MONTHS);
            put("YEARLY", ChronoUnit.YEARS);
        }});

    private GoogleCalendar() {
    }

    /**
     * Converts an Event into a Google Calendar Event.
     * If the event is recurring, the conversion can only happen if the event is the first to occur in the recurrence,
     * else an exception is thrown.
     * Event class of the Google Calendar API : https://developers.google.com/resources/api-libraries/documentation/calendar/v3/java/latest/
     * @param event (Event): the Event to convert into a Google Calendar Event
     * @return (com.google.api.services.calendar.model.Event): a Google Calendar Event
     */
    public static com.google.api.services.calendar.model.Event toGCalendarEvents(Event event) {
        Objects.requireNonNull(event);

        com.google.api.services.calendar.model.Event gcEvent = new com.google.api.services.calendar.model.Event()
                .setSummary(event.getName())
                .setId(event.getId().getUuid());

        if(event.isRecurrent()) {
            Recurrence recurrence = event.getRecurrence().get();
            if(!event.getId().equals(recurrence.getGroupId())) {
                throw new IllegalArgumentException("Cannot convert a recurring event to a google event if this one is not the first element of the recurrence");
            }
            gcEvent.setRecurringEventId(recurrence.getGroupId().getUuid());

            String recurrencePeriod = null;
            for(Map.Entry<String, TemporalUnit> t : PERIODS.entrySet()) {
                if(recurrence.getPeriod().equals(t.getValue().getDuration())) {
                    recurrencePeriod = t.getKey();
                }
            }
            if(recurrencePeriod == null) {
                throw new IllegalArgumentException("The event recurrence period does not match any possible periods proposed.");
            }

            long count = (recurrence.getEndTime().toEpochSecond() - event.getStartTime().toEpochSecond()) / recurrence.getPeriod().getSeconds() + 1;

            String[] recurr = new String[] {"RRULE:FREQ="+recurrencePeriod+";COUNT="+count};
            gcEvent.setRecurrence(Arrays.asList(recurr));
        }

        if(event.getLocation().isPresent()) {
            gcEvent.setLocation(event.getLocationName());
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

        return gcEvent;
    }

    /**
     * Converts a Google Calendar Event into an Event
     * Note: The resulting new Event doesn't have a location specified with coordinates, so technically it doesn't have
     * a location. The only information we have on a potential location is the locationName.
     * The id of the new Event is a new (generated) Id, because one cannot be sure that the Google Calendar Event
     * id is in the same format as the local Event id UUID format.
     * @param event (com.google.api.services.calendar.model.Event) : The Google Calendar Event to convert into an Event
     * @return (Event) : a new Event
     */
    public static Event fromGCalendarEvents(com.google.api.services.calendar.model.Event event) {
        Objects.requireNonNull(event);
        if(event.getStart() == null || event.getEnd() == null) {
            throw new IllegalArgumentException("Event should have at least a start time and an end time.");
        }

        Id newId = Id.generateRandom();
        String name = (event.getSummary() != null) ? event.getSummary() : DEFAULT_NAME;
        String locationName = (event.getLocation() != null) ? event.getLocation() : "";

        long epochSecondStartTime = event.getStart().getDateTime().getValue()/1000;
        ZonedDateTime startTime = TimeUtils.epochToZonedDateTime(epochSecondStartTime);
        long epochSecondEndTime = event.getEnd().getDateTime().getValue()/1000;
        ZonedDateTime endTime = TimeUtils.epochToZonedDateTime(epochSecondEndTime);

        Event newEvent = new Event(newId, name, locationName, startTime, endTime);

        if(event.getRecurrence() != null) {
            List<String> recurrences = event.getRecurrence();
            String periodically = null;
            int eventCount = 0;
            boolean rule_found = false;
            String ruleFoundString = "";

            for(String rule : recurrences) {
                //Refer to https://developers.google.com/calendar/create-events to see how to retrieve recurrence
                if(rule.substring(0, 5).equals("RRULE")) {
                    if(rule_found && !rule.equals(ruleFoundString)) {
                        throw new IllegalArgumentException("There are two different matching rule formats for the recurrence.");
                    }
                    String[] info = rule.substring(11).split(";COUNT=");
                    periodically = info[0];
                    eventCount = Integer.parseInt(info[1]);
                    rule_found = true;
                    ruleFoundString = rule;
                }
            }
            if(periodically == null) {
                throw new IllegalArgumentException("No recurrence rule matches the one used by "+ R.string.app_name+" Events");
            }

            TemporalUnit period = PERIODS.getOrDefault(periodically, null);
            if(period == null) {
                throw new IllegalArgumentException("The event recurrence period does not match any possible periods proposed.");
            }

            ZonedDateTime recEndTime = TimeUtils.epochToZonedDateTime
                    (startTime.toEpochSecond() + (eventCount-1) * period.getDuration().getSeconds());

            Recurrence newRecurrence = new Recurrence(newEvent.getId(), period.getDuration(), recEndTime);
            newEvent = newEvent.setRecurrence(newRecurrence);
        }

        return newEvent;
    }

    public static CompletableFuture<Void> exportEvents(Context ctx, Account account, CompletableFuture<List<com.github.onedirection.event.Event>> ls) {
        // TODO: Task #206 -- See `gcalendar-export` branch for WIP implementation
        return ls.thenAccept(ignored -> {
        });
    }
}
