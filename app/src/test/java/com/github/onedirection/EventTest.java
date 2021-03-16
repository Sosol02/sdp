package com.github.onedirection;

import com.github.onedirection.geocoding.NamedCoordinates;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class EventTest {

    private final static int ID = 001;
    private final static String NAME = "Event name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusHours(1);

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

    @Test
    public void testEventWithNullArgument() {
        assertThrows(NullPointerException.class, () -> new Event(ID, null, LOCATION, START_TIME, END_TIME));
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, null, START_TIME, END_TIME));
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, LOCATION, null, END_TIME));
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, LOCATION, START_TIME, null));
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
        Event eventChanged = EVENT.setLocation(newLoc);
        assertEquals(newLoc, eventChanged.getLocation());
        assertEquals(LOCATION, EVENT.getLocation());
        assertThat(EVENT.setLocation(LOCATION), sameInstance(EVENT));
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
}