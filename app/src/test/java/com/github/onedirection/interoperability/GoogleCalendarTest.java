package com.github.onedirection.interoperability;

import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.model.Recurrence;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.interoperability.gcalendar.GoogleCalendar;
import com.github.onedirection.utils.Id;

import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GoogleCalendarTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "EVENT";
    private final static String LOCATION = "Location";
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    private final static ZonedDateTime END_TIME = START_TIME.plusHours(1);
    private static final Event EVENT = new Event(
            ID,
            NAME,
            LOCATION,
            START_TIME,
            END_TIME,
            new Recurrence(ID, ChronoUnit.WEEKS.getDuration(), START_TIME.plusWeeks(3)), false
    );

    private static final Event GEO_EVENT = new Event(
            ID,
            NAME,
            new NamedCoordinates(0, 0, LOCATION),
            START_TIME,
            END_TIME,
            new Recurrence(ID, ChronoUnit.WEEKS.getDuration(), START_TIME.plusWeeks(3)), false
    );

    @Test
    public void toGCalendarEventConvertsStartEndProperly() {
        com.google.api.services.calendar.model.Event gcEvent = GoogleCalendar.toGCalendarEvents(EVENT);

        assertEquals(START_TIME.toEpochSecond(), gcEvent.getStart().getDateTime().getValue() / 1000);
        assertEquals(END_TIME.toEpochSecond(), gcEvent.getEnd().getDateTime().getValue() / 1000);
    }

    @Test
    public void toGCalendarEventConvertsNameAndIdAndLocationName() {
        com.google.api.services.calendar.model.Event gcEvent = GoogleCalendar.toGCalendarEvents(EVENT);

        assertEquals(NAME, gcEvent.getSummary());
        assertEquals(LOCATION, gcEvent.getLocation());
    }

    @Test
    public void toGCalendarEventConvertsRecurrenceFirstOccurrence() {
        com.google.api.services.calendar.model.Event gcEvent = GoogleCalendar.toGCalendarEvents(EVENT);
        String[] recurrence = gcEvent.getRecurrence().get(0).split(";");

        assertEquals(ID.getUuid(), gcEvent.getRecurringEventId());
        assertEquals("WEEKLY", recurrence[0].substring(11));
        assertEquals("4", recurrence[1].substring(6));
    }

    @Test
    public void conversionBijection() {
        com.google.api.services.calendar.model.Event gcEvent = GoogleCalendar.toGCalendarEvents(EVENT);
        Event event = GoogleCalendar.fromGCalendarEvents(gcEvent);

        assertEquals(EVENT.getId(), event.getId());
        assertEquals(NAME, event.getName());
        assertEquals(LOCATION, event.getLocationName());
        assertEquals(START_TIME, event.getStartTime());
        assertEquals(END_TIME, event.getEndTime());
        assertEquals(ChronoUnit.WEEKS.getDuration(), event.getRecurrence().get().getPeriod());
        assertEquals(START_TIME.plusWeeks(3), event.getRecurrence().get().getEndTime());

        assertEquals(EVENT, event);
    }

    @Ignore("Missing feature")
    @Test
    public void conversionBijectionGeolocation() {
        com.google.api.services.calendar.model.Event gcEvent = GoogleCalendar.toGCalendarEvents(GEO_EVENT);
        Event event = GoogleCalendar.fromGCalendarEvents(gcEvent);

        assertEquals(GEO_EVENT, event);
    }

    @Ignore("Missing feature")
    @Test
    public void conversionBijectionRecurrence() {
        Id id = Id.generateRandom();
        Recurrence rec = new Recurrence(id, Duration.ofDays(1), ZonedDateTime.now().plusDays(3));
        Event recRoot =
                new Event(id, "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);

        Event e = GoogleCalendar.fromGCalendarEvents(GoogleCalendar.toGCalendarEvents(recRoot));
        //assertThat(e, hasSize(3));
    }

    @Test
    public void eventsFilteringFilterCorrectly() {
        Id id = Id.generateRandom();
        Recurrence rec = new Recurrence(id, Duration.ofSeconds(1), ZonedDateTime.now());
        Event recRoot = new Event(id, "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);
        Event rec1 = new Event(Id.generateRandom(), "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);
        Event rec2 = new Event(Id.generateRandom(), "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);

        List<Event> events = Arrays.asList(EVENT, recRoot, rec1, rec2);
        List<Event> result = GoogleCalendar.removeRecurrent(events);

        assertThat(result, is(Arrays.asList(EVENT, recRoot)));
    }

    @Test
    public void eventsFilteringThrowsOnMissingRoot() {
        Id id = Id.generateRandom();
        Recurrence rec = new Recurrence(id, Duration.ofSeconds(1), ZonedDateTime.now());
        Event rec1 = new Event(Id.generateRandom(), "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);
        Event rec2 = new Event(Id.generateRandom(), "Event", "", ZonedDateTime.now(), ZonedDateTime.now(), rec, false);

        List<Event> events = Arrays.asList(EVENT, rec1, rec2);
        assertThrows(IllegalArgumentException.class, () -> GoogleCalendar.removeRecurrent(events));

    }
}
