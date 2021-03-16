package com.github.onedirection;

import com.github.onedirection.geocoding.NamedCoordinates;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class EventTest {

    private final static int ID = 001;
    private final static String NAME = "Event name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, "Location name");
    private final static ZonedDateTime START_TIME = ZonedDateTime.now();
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusHours(1);

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

    @Test
    public void testEventWithNullArgument() {
        int id = 001;
        String name = "name";
        NamedCoordinates location = new NamedCoordinates(0, 0, "name");
        ZonedDateTime start_time = ZonedDateTime.now();
        ZonedDateTime end_time = ZonedDateTime.now().plusHours(1);

        assertThrows(NullPointerException.class, () -> new Event(id, null, location, start_time, end_time));
        assertThrows(NullPointerException.class, () -> new Event(id, name, null, start_time, end_time));
        assertThrows(NullPointerException.class, () -> new Event(id, name, location, null, end_time));
        assertThrows(NullPointerException.class, () -> new Event(id, name, location, start_time, null));
    }

    @Test
    public void testEventSetNameAndGet() {
        final String newName = "changed_parameter";

        assertThrows(NullPointerException.class, () -> EVENT.setName(null));
        Event eventChanged = EVENT.setName(newName);
        assertEquals(newName, eventChanged.getName());
        assertEquals(NAME, EVENT.getName());
    }

    @Test
    public void testEventSetLocationAndGet() {
        final NamedCoordinates newLoc = new NamedCoordinates(1, 1, "New location name");

        assertThrows(NullPointerException.class, () -> EVENT.setLocation(null));
        Event eventChanged = EVENT.setLocation(newLoc);
        assertEquals(newLoc, eventChanged.getLocation());
        assertEquals(LOCATION, EVENT.getLocation());
    }

    @Test
    public void testEventSetStartTimeAndGet() {
        final ZonedDateTime newTime = ZonedDateTime.now().minusHours(1);

        assertThrows(NullPointerException.class, () -> EVENT.setStartTime(null));
        Event eventChanged = EVENT.setStartTime(newTime);
        assertEquals(newTime, eventChanged.getStartTime());
        assertEquals(START_TIME, EVENT.getStartTime());
    }

    @Test
    public void testEventSetEndTimeAndGet() {
        final ZonedDateTime newTime = ZonedDateTime.now().plusYears(1);

        assertThrows(NullPointerException.class, () -> EVENT.setEndTime(null));
        Event eventChanged = EVENT.setEndTime(newTime);
        assertEquals(newTime, eventChanged.getEndTime());
        assertEquals(START_TIME, EVENT.getStartTime());
    }
}