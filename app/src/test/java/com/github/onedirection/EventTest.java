package com.github.onedirection;

import com.github.onedirection.events.Event;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class EventTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
    private final static Duration DURATION = Duration.of(1, ChronoUnit.HOURS);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plus(DURATION).truncatedTo(Event.TIME_PRECISION);
    private final static Recurrence RECURRING_PERIOD = new Recurrence(Id.generateRandom(), Duration.ofDays(1), END_TIME, Optional.of(Id.generateRandom()), Optional.of(Id.generateRandom())); //Daily

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);

    @Test
    public void testEventWithNullArgument() {
        assertThrows(NullPointerException.class, () -> new Event(ID, null, LOCATION, START_TIME, END_TIME));
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, LOCATION, null, END_TIME));
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, LOCATION, START_TIME, null));
    }

    @Test
    public void idIsCorrect(){
        assertThat(EVENT.getId(), is(ID));
    }

    @Test
    public void testEventSetNameAndGet() {
        final String newName = "New event name";

        assertThrows(NullPointerException.class, () -> EVENT.setName(null));
        Event eventChanged = EVENT.setName(newName);
        assertEquals(newName, eventChanged.getName());
        assertEquals(NAME, EVENT.getName());
        assertThat(EVENT.setName(NAME), sameInstance(EVENT));
    }

    @Test
    public void testEventSetLocationAndGet() {
        final NamedCoordinates newLoc = new NamedCoordinates(1, 1, "New location name");

        assertThrows(NullPointerException.class, () -> EVENT.setLocation(null));
        assertThat(EVENT.getLocationName(), is(LOCATION.name));
        Event eventChanged = EVENT.setLocation(newLoc);
        assertEquals(Optional.of(newLoc), eventChanged.getLocation());
        assertThat(eventChanged.getLocationName(), is(newLoc.name));
        assertEquals(Optional.of(LOCATION), EVENT.getLocation());
        assertThat(EVENT.setLocation(LOCATION), sameInstance(EVENT));

        assertThat(eventChanged.getCoordinates(), is(Optional.of(newLoc.dropName())));
    }

    @Test
    public void testEventSetStartTimeAndGet() {
        final ZonedDateTime newTime = ZonedDateTime.now().minusHours(1).truncatedTo(Event.TIME_PRECISION);

        assertThrows(NullPointerException.class, () -> EVENT.setStartTime(null));
        Event eventChanged = EVENT.setStartTime(newTime);
        assertEquals(newTime, eventChanged.getStartTime());
        assertEquals(START_TIME, EVENT.getStartTime());
        assertThat(EVENT.setStartTime(START_TIME), sameInstance(EVENT));
    }

    @Test
    public void testEventSetRecurringPeriodAndGet() {
        final Recurrence recurringPeriod = new Recurrence(RECURRING_PERIOD.getGroupId(), Duration.ofDays(7), END_TIME, RECURRING_PERIOD.getPrevEvent(), RECURRING_PERIOD.getNextEvent());

        assertThrows(NullPointerException.class, () -> EVENT.setRecurrence(null));
        Event eventChanged = EVENT.setRecurrence(recurringPeriod);
        assertTrue(eventChanged.getRecurrence().isPresent());
        assertEquals(Optional.of(recurringPeriod), eventChanged.getRecurrence());
        assertEquals(Optional.of(RECURRING_PERIOD), EVENT.getRecurrence());
        assertThat(EVENT.setRecurrence(RECURRING_PERIOD), sameInstance(EVENT));
    }

    @Test
    public void testEventSetEndTimeAndGet() {
        final ZonedDateTime newTime = ZonedDateTime.now().plusYears(1).truncatedTo(Event.TIME_PRECISION);

        assertThrows(NullPointerException.class, () -> EVENT.setEndTime(null));
        Event eventChanged = EVENT.setEndTime(newTime);
        assertEquals(newTime, eventChanged.getEndTime());
        assertEquals(START_TIME, EVENT.getStartTime());
        assertThat(EVENT.setEndTime(END_TIME), sameInstance(EVENT));
    }

    @Test
    public void eventCannotEndBeforeStarting(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event(
                        ID, NAME, LOCATION, START_TIME, START_TIME.minus(1, Event.TIME_PRECISION)
                )
        );
    }

    @Test
    public void eventCanEndInstantly(){
        Event event = new Event(ID, NAME, LOCATION, START_TIME, START_TIME);
        assertThat(event.getStartTime(), is(event.getEndTime()));
    }

    @Test
    public void toStringContainsAllFields(){
        String str = EVENT.toString();
        assertThat(str, containsString(ID.toString()));
        assertThat(str, containsString(NAME));
        assertThat(str, containsString(LOCATION.dropName().toString()));
        assertThat(str, containsString(LOCATION.name));
        assertThat(str, containsString(START_TIME.toString()));
        assertThat(str, containsString(END_TIME.toString()));
        assertThat(str, containsString(RECURRING_PERIOD.toString()));
    }

    @Test
    public void equalsBehavesAsExpected(){
        Event event1 = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);
        Event event2 = new Event(Id.generateRandom(), NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);
        Event event3 = new Event(ID, NAME, LOCATION, START_TIME, END_TIME.plusHours(1), RECURRING_PERIOD);
        Event event4 = new Event(ID, NAME, LOCATION.name, START_TIME, END_TIME, RECURRING_PERIOD);
        Event event5 = new Event(ID, NAME, LOCATION.name, LOCATION.dropName(), START_TIME, END_TIME, RECURRING_PERIOD);
        Event event6 = new Event(ID, NAME, "Another name", LOCATION.dropName(), START_TIME, END_TIME, RECURRING_PERIOD);
        Event event7 = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD.setPeriod(RECURRING_PERIOD.getPeriod().plusMinutes(1)));
        assertThat(EVENT, is(EVENT));
        assertThat(EVENT, is(event1));
        assertThat(EVENT, not(is(1)));
        assertThat(EVENT, not(is(event2)));
        assertThat(EVENT, not(is(event3)));
        assertThat(EVENT, not(is(event4)));
        assertThat(EVENT, is(event5));
        assertThat(EVENT, not(is(event6)));
        assertThat(EVENT, not(is(event7)));
    }

    @Test
    public void hashCodeIsEqualCompatible(){
        Event event = new Event(ID, NAME, LOCATION, START_TIME, END_TIME, RECURRING_PERIOD);
        assertThat(event, is(EVENT));
        assertThat(event, not(sameInstance(EVENT)));
        assertThat(event.hashCode(), is(EVENT.hashCode()));
    }

    @Test
    public void durationIsCorrect(){
        assertThat(EVENT.getDuration(), is(DURATION));
    }
}